const parse = require('parse-diff');

const diffText = `
--- a/app/backend/src/main/java/com/lz/inno_dev/controller/GitRepositoryWebhookController.java
+++ b/app/backend/src/main/java/com/lz/inno_dev/controller/GitRepositoryWebhookController.java
 }
`;

const files = parse(diffText);

files.forEach(file => {
    console.log(`File: ${file.from} -> ${file.to}`);
    file.chunks.forEach(chunk => {
        console.log(`  Chunk: ${chunk.oldStart},${chunk.oldLines} -> ${chunk.newStart},${chunk.newLines}`);
        chunk.changes.forEach(change => {
            console.log(`    ${change.type} ${change.content}`);
        });
    });
});