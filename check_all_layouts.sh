#!/bin/bash

echo "===================================="
echo "检查所有Fragment布局文件"
echo "===================================="

layouts=(
    "fragment_today.xml"
    "fragment_hourly.xml"
    "fragment_forecast15d.xml"
    "fragment_main_cities.xml"
)

for layout in "${layouts[@]}"; do
    layout_path="app/src/main/res/layout/$layout"
    
    if [ -f "$layout_path" ]; then
        echo ""
        echo "📄 检查: $layout"
        echo "------------------------------------"
        
        # 查找可能缺少layout_width的TextView
        grep -n "<TextView" "$layout_path" | while read line; do
            line_num=$(echo "$line" | cut -d: -f1)
            # 检查这个TextView的完整定义（包括接下来的几行）
            context=$(sed -n "${line_num},$((line_num+10))p" "$layout_path" | sed '/\/>/q')
            
            # 检查是否有layout_width
            if ! echo "$context" | grep -q "layout_width"; then
                # 检查是否使用了style（style可能包含layout_width）
                if echo "$context" | grep -q "style="; then
                    echo "  ℹ️  第 $line_num 行使用了style（可能包含layout_width）"
                else
                    echo "  ⚠️  第 $line_num 行可能缺少layout_width"
                fi
            fi
        done
        
        # XML格式验证
        if command -v xmllint &> /dev/null; then
            xmllint --noout "$layout_path" 2>&1 | grep -v "^$" | while read error; do
                echo "  ❌ XML错误: $error"
            done
        fi
    else
        echo ""
        echo "📄 $layout - 文件不存在（可能尚未创建）"
    fi
done

echo ""
echo "===================================="
echo "检查完成！"
echo "===================================="

