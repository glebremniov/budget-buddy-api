Sync the local `src/main/resources/openapi.yaml` to the Postman "Budget Buddy API" spec.

**Postman spec details (do not look these up, use as-is):**
- Spec ID: `f1ef247c-b7e9-4a36-a4e8-1072ed7222d1`
- Root file path: `1.0.1.yaml`
- Workspace: "My Workspace" (`349d686a-722f-46a2-8517-fca3e6fbd3f6`)

**Instructions:**
1. Read `src/main/resources/openapi.yaml`
2. Read the current Postman root spec using `mcp__MCP_DOCKER__getSpecDefinition` (specId: `f1ef247c-b7e9-4a36-a4e8-1072ed7222d1`) to identify any content in Postman that is NOT in the local file (e.g. budget endpoints) — preserve it
3. Merge: apply all changes from the local file into the Postman spec, keeping any extra Postman-only content intact
4. Push the merged content using `mcp__MCP_DOCKER__updateSpecFile` (specId: `f1ef247c-b7e9-4a36-a4e8-1072ed7222d1`, filePath: `1.0.1.yaml`)
5. Confirm what was updated
