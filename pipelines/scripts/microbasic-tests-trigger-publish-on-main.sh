#!/usr/bin/env bash
set -euo pipefail

if [ "${CM_BRANCH}" != "main" ]; then
  echo "Branch '${CM_BRANCH}' is not main. Skipping publish trigger."
  exit 0
fi

if [ -z "${CM_API_TOKEN:-}" ] || [ -z "${CM_APP_ID:-}" ]; then
  echo "CM_API_TOKEN and CM_APP_ID are required in codemagic_api_credentials."
  exit 1
fi

payload=$(cat <<JSON
{
  "appId": "${CM_APP_ID}",
  "workflowId": "${DEPLOY_WORKFLOW_ID}",
  "branch": "main"
}
JSON
)

echo "Triggering workflow '${DEPLOY_WORKFLOW_ID}' for branch main."
curl -sS -f \
  -H "Content-Type: application/json" \
  -H "x-auth-token: ${CM_API_TOKEN}" \
  --data "${payload}" \
  -X POST https://api.codemagic.io/builds
