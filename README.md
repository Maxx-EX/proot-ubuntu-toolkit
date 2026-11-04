# PRoot Ubuntu Toolkit

免 Root 的 Android 端 Ubuntu 工作台。

- **APK**：PRoot 引导器 + C 语言 JNI 底层工具 + Kotlin/Java UI
- **OBB**：完整 Ubuntu rootfs（debootstrap 构建，预装 build-essential / git / python3 等）
- **分发**：自签名 APK + OBB，不上架应用商店
- **CI**：GitHub Actions 自动构建 APK 与 OBB

## 目录结构
```

.github/workflows/   # CI 工作流
scripts/             # rootfs 构建脚本
obb/                 # OBB 打包输入（CI 中生成）
app/                 # Android 项目（待填充）

```
