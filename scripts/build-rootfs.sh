#!/usr/bin/env bash
# 在 GitHub Actions 的 ubuntu-latest 上构建 ARM64 Ubuntu rootfs
# 用法：bash scripts/build-rootfs.sh <ubuntu代号> <输出目录>
set -euo pipefail

RELEASE="${1:-jammy}"
OUTDIR="${2:-obb_input}"

sudo apt-get update
sudo apt-get install -y debootstrap qemu-user-static binfmt-support

sudo rm -rf "$OUTDIR"
sudo mkdir -p "$OUTDIR"

# 第一阶段：最小系统
sudo debootstrap --arch=arm64 --foreign "$RELEASE" "$OUTDIR" \
  http://ports.ubuntu.com/ubuntu-ports/

# 第二阶段：在 qemu 下完成配置并预装工具链
sudo cp /usr/bin/qemu-aarch64-static "$OUTDIR/usr/bin/"
sudo chroot "$OUTDIR" /debootstrap/debootstrap --second-stage

sudo chroot "$OUTDIR" bash -c '
  export DEBIAN_FRONTEND=noninteractive
  apt-get update
  apt-get install -y --no-install-recommends \
    build-essential gcc g++ make cmake git curl wget \
    python3 python3-pip vim nano less tree \
    ca-certificates openssh-client
  apt-get clean
  rm -rf /var/lib/apt/lists/*
'

echo "rootfs 构建完成，大小："
sudo du -sh "$OUTDIR"
