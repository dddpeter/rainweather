#!/bin/bash

# 检查fragment_today.xml中所有TextView是否都有必需的属性
echo "正在检查fragment_today.xml布局..."

layout_file="app/src/main/res/layout/fragment_today.xml"

# 使用xmllint验证XML（如果可用）
if command -v xmllint &> /dev/null; then
    echo "验证XML格式..."
    xmllint --noout "$layout_file" 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ XML格式正确"
    else
        echo "❌ XML格式有问题"
    fi
fi

# 查找所有没有layout_width的View元素
echo ""
echo "查找缺少layout_width的元素..."
grep -n "android:text=\"" "$layout_file" | while read line; do
    line_num=$(echo "$line" | cut -d: -f1)
    # 检查前后5行是否有layout_width
    sed -n "$((line_num-5)),$((line_num+5))p" "$layout_file" | grep -q "layout_width"
    if [ $? -ne 0 ]; then
        echo "⚠️  第 $line_num 行附近可能缺少layout_width属性"
        echo "   $line"
    fi
done

echo ""
echo "检查完成！"

