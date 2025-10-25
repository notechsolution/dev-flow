<script lang="ts" setup>
import {Milkdown, useEditor} from "@milkdown/vue";
import {Crepe} from "@milkdown/crepe";
import '@milkdown/crepe/theme/common/style.css';
import "@milkdown/crepe/theme/frame.css";
import {ListenerManager} from "@milkdown/plugin-listener";
import {defineEmits, watch} from "vue";
import type {DefaultValue} from "@milkdown/kit/lib/core";
import isHtml from 'is-html';
import {replaceAll} from "@milkdown/kit/utils";
import TurndownService from 'turndown';
import {ElMessage} from "element-plus";
// import imageApi from "@/api/image-api";

const emit = defineEmits(['updatedContent']);
let crepe: Crepe;
let contentUpdated = false;

const props = defineProps({
    content: {
        type: String,
        default: ''
    },
    readonly: {
        type: Boolean,
        default: false
    },
    enableCopy: {
        type: Boolean,
        default: true
    },
    placeholder: {
        type: String,
        default: 'Please input User Story Description here...',
        required: false
    }
});

watch(() => props.content, (value: string) => {
    if (!contentUpdated) {
        updateDefaultValue(value)
    }
}, {once: true});

watch(() => props.readonly, (value: boolean) => {
    updateReadyOnly(value)
});

const specialHandleCodeBlock = (dom: HTMLElement) => {
    // handle html between <pre> and <code> tags
    const preTags = dom.querySelectorAll('pre');
    preTags.forEach(pre => {
        const codeTags = pre.querySelectorAll('code');
        codeTags.forEach(code => {
            // Remove div
            const divs = code.querySelectorAll('div');
            divs.forEach(div => {
                while (div.firstChild) {
                    div.parentNode?.insertBefore(div.firstChild, div);
                }
                div.parentNode?.removeChild(div);
            });

            // Replace <br> with \n
            const brs = code.querySelectorAll('br');
            brs.forEach(br => {
                const textNode = document.createTextNode('\n');
                br.parentNode?.replaceChild(textNode, br);
            });
        });
    });
    return dom;
}

const editorRef = useEditor((root) => {
    let defaultValue = props.content as DefaultValue;

    if (isHtml(props.content)) {
        const parser = new DOMParser();
        const doc = parser.parseFromString(props.content, 'text/html');
        const dom = specialHandleCodeBlock(doc.body);
        defaultValue = {
            type: "html",
            dom: dom
        }
    }
    crepe = new Crepe({
        root,
        defaultValue,
        features: {[Crepe.Feature.Placeholder]: true},
        featureConfigs: {
            [Crepe.Feature.Placeholder]: {
                text: props.placeholder
            },
            // [Crepe.Feature.ImageBlock]: {
                // blockOnUpload: async (file: File) => {
                //     const res = await imageApi.uploadImage(file);
                //     return res.data;
                // }
            // }
        },
    });

    crepe.setReadonly(props.readonly);

    crepe.on((api: ListenerManager) => {
        api.markdownUpdated((ctx, markdown, prevMarkdown) => {
            contentUpdated = true;
            emit('updatedContent', markdown);
        });
    })

    crepe.on((api: ListenerManager) => {
        api.destroy((ctx) => {
            console.log('destroyed');
        });
    })

    crepe.create().then(() => {
        updateDefaultValue(props.content)
    });

    return crepe;
})

const updateDefaultValue = (value: string) => {
    const editor = editorRef.get();
    if (isHtml(value)) {
        const turndownService = new TurndownService();
        value = turndownService.turndown(value);
    }
    editor?.action(replaceAll(value, true))
}

const updateReadyOnly = (value: boolean) => {
    if (crepe) {
        crepe.setReadonly(value);
    }
}

const copy = () => {
    navigator.clipboard.writeText(props.content)
    ElMessage.success('Copied to clipboard')
}

defineExpose({
    updateDefaultValue
})
</script>

<template>
    <div class="relative h-full w-full py-2 milkdown-div">
        <Milkdown class="text-start milkdown-container h-full"/>
        <el-button v-if="enableCopy" class="hidden absolute top-4 right-2 copy" type="primary" @click="copy">Copy
        </el-button>
    </div>
</template>

<style scoped>
.milkdown-container {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    padding: 10px;
    border-radius: 5px;
    min-height: 300px;
}

:deep(.milkdown .ProseMirror) {
    padding: 10px 10px 10px 70px;
}

.milkdown-div:hover {
    .copy {
        display: block;
    }
}
</style>
