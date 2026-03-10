#!/usr/bin/env bash
# =============================================================================
# setup-docker.sh
#
# Installs Docker Engine on Ubuntu following the official Docker documentation:
#   - https://docs.docker.com/engine/install/ubuntu/
#   - https://docs.docker.com/engine/install/linux-postinstall/
#
# Usage:
#   chmod +x scripts/setup-docker.sh
#   ./scripts/setup-docker.sh
# =============================================================================

set -euo pipefail

# -----------------------------------------------------------------------------
# Colors for output
# -----------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step()  { echo -e "\n${BLUE}==>${NC} $1"; }
print_ok()    { echo -e "${GREEN}[OK]${NC} $1"; }
print_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# -----------------------------------------------------------------------------
# Guard: must run on Ubuntu
# -----------------------------------------------------------------------------
if [[ ! -f /etc/os-release ]] || ! grep -qi "ubuntu" /etc/os-release; then
  print_error "This script is intended for Ubuntu only."
  exit 1
fi

# -----------------------------------------------------------------------------
# Guard: must not run as root
# -----------------------------------------------------------------------------
if [[ "$EUID" -eq 0 ]]; then
  print_error "Do not run this script as root. Run as your normal user; sudo will be called as needed."
  exit 1
fi

echo -e "${BLUE}"
echo "============================================="
echo "  Notes Vault API — Docker Setup"
echo "  Following official Docker Ubuntu docs"
echo "============================================="
echo -e "${NC}"

# -----------------------------------------------------------------------------
# Helper: check if Docker is from the official Docker repository
# Returns 0 (true) if official, 1 (false) if not
# -----------------------------------------------------------------------------
is_official_docker() {
  # Official Docker packages are named docker-ce, not docker.io
  dpkg -l docker-ce &>/dev/null 2>&1
}

# -----------------------------------------------------------------------------
# Helper: check if docker compose plugin is available
# -----------------------------------------------------------------------------
has_compose_plugin() {
  docker compose version &>/dev/null 2>&1
}

# -----------------------------------------------------------------------------
# STEP 1: Detect current Docker state
# -----------------------------------------------------------------------------
print_step "Detecting Docker installation state..."

NEEDS_INSTALL=false
NEEDS_REMOVAL=false

if command -v docker &>/dev/null; then
  DOCKER_VERSION=$(docker --version)
  print_ok "Docker found: ${DOCKER_VERSION}"

  if is_official_docker; then
    print_ok "Source: Official Docker repository (docker-ce)"
    if has_compose_plugin; then
      print_ok "docker compose plugin is available."
      NEEDS_INSTALL=false
    else
      print_warn "docker compose plugin is missing. Will reinstall from official repo."
      NEEDS_INSTALL=true
      NEEDS_REMOVAL=true
    fi
  else
    print_warn "Source: Ubuntu system packages (docker.io) — does NOT include docker compose plugin."
    NEEDS_INSTALL=true
    NEEDS_REMOVAL=true
  fi
else
  print_warn "Docker is not installed."
  NEEDS_INSTALL=true
fi

# -----------------------------------------------------------------------------
# STEP 2: Prompt user before removing old Docker
# -----------------------------------------------------------------------------
if [[ "$NEEDS_REMOVAL" == true ]]; then
  echo ""
  echo -e "${YELLOW}The following conflicting packages will be removed:${NC}"
  echo "  docker.io, docker-compose, docker-compose-v2, docker-doc,"
  echo "  podman-docker, containerd, runc"
  echo ""
  echo -e "${YELLOW}Any existing containers, images, and volumes will be preserved${NC}"
  echo -e "${YELLOW}in /var/lib/docker unless you manually delete them.${NC}"
  echo ""
  read -rp "Do you want to proceed? [y/N] " response
  case "$response" in
    [yY][eE][sS]|[yY])
      print_ok "Proceeding with removal of old Docker packages."
      ;;
    *)
      print_error "Aborted by user."
      exit 0
      ;;
  esac
fi

# -----------------------------------------------------------------------------
# STEP 3: Remove conflicting packages (if needed)
# -----------------------------------------------------------------------------
if [[ "$NEEDS_REMOVAL" == true ]]; then
  print_step "Removing conflicting Docker packages..."

  CONFLICTING=(docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc)
  for pkg in "${CONFLICTING[@]}"; do
    if dpkg -l "$pkg" &>/dev/null 2>&1; then
      sudo apt remove -y "$pkg"
      print_ok "Removed: $pkg"
    else
      echo "  Not installed (skip): $pkg"
    fi
  done
fi

# -----------------------------------------------------------------------------
# STEP 4: Install official Docker Engine (if needed)
# -----------------------------------------------------------------------------
if [[ "$NEEDS_INSTALL" == true ]]; then
  print_step "Setting up Docker's official apt repository..."

  sudo apt update
  sudo apt install -y ca-certificates curl

  sudo install -m 0755 -d /etc/apt/keyrings
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    -o /etc/apt/keyrings/docker.asc
  sudo chmod a+r /etc/apt/keyrings/docker.asc

  sudo tee /etc/apt/sources.list.d/docker.sources > /dev/null <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Signed-By: /etc/apt/keyrings/docker.asc
EOF

  sudo apt update
  print_ok "Docker apt repository configured."

  print_step "Installing Docker Engine, CLI, and plugins..."

  sudo apt install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin

  print_ok "Docker packages installed."
fi

# -----------------------------------------------------------------------------
# STEP 5: Post-install — manage Docker as a non-root user
# -----------------------------------------------------------------------------
print_step "Configuring docker group for non-root access..."

if getent group docker > /dev/null 2>&1; then
  print_ok "docker group already exists."
else
  sudo groupadd docker
  print_ok "Created docker group."
fi

if id -nG "$USER" | grep -qw docker; then
  print_ok "${USER} is already in the docker group."
else
  sudo usermod -aG docker "$USER"
  print_ok "Added ${USER} to the docker group."
fi

# -----------------------------------------------------------------------------
# Helper: detect if running inside WSL
# -----------------------------------------------------------------------------
is_wsl() {
  grep -qi microsoft /proc/version 2>/dev/null
}

# -----------------------------------------------------------------------------
# STEP 6: Post-install — enable Docker to start on boot
# -----------------------------------------------------------------------------
print_step "Enabling Docker to start on boot..."

if is_wsl; then
  echo ""
  print_warn "WSL environment detected."
  print_warn "systemd service management is not supported in WSL2."
  echo ""
  echo "  To properly enable Docker on boot, please run this script"
  echo "  in a native Ubuntu terminal (not WSL)."
  echo ""
  echo "  In the meantime, you can start Docker manually with:"
  echo "    sudo service docker start"
  echo ""
else
  sudo systemctl enable docker.service
  sudo systemctl enable containerd.service
  sudo systemctl start docker.service
  print_ok "docker.service and containerd.service enabled and started."
fi

# -----------------------------------------------------------------------------
# STEP 7: Verify installation
# -----------------------------------------------------------------------------
print_step "Verifying Docker installation..."

DOCKER_VERSION=$(docker --version)
COMPOSE_VERSION=$(docker compose version)
print_ok "Docker:         ${DOCKER_VERSION}"
print_ok "Docker Compose: ${COMPOSE_VERSION}"

print_step "Running hello-world container..."
sudo docker run --rm hello-world

# -----------------------------------------------------------------------------
# Done
# -----------------------------------------------------------------------------
echo -e "\n${GREEN}============================================="
echo "  Docker setup complete!"
echo "=============================================${NC}"
echo ""
echo "  IMPORTANT: To run docker without sudo, you must either:"
echo "    1. Log out and log back in, OR"
echo "    2. Run: newgrp docker"
echo ""
echo "  Then you can run: docker compose up --build"
echo ""