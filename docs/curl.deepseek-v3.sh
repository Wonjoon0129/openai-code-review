curl -X POST https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions \
-H "Authorization: Bearer sk-db2968be21644311a3ceca2de967552b" \
-H "Content-Type: application/json" \
-d '{
    "model": "deepseek-v3",
    "messages": [
        {
            "role": "user",
            "content": "9.9和9.11谁大"
        }
    ]
}'