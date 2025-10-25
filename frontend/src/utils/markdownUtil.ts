import TurndownService from 'turndown';

const convertHtmlToMarkdown = (htmlContent: string): string => {
    const turndownService = new TurndownService()
    return turndownService.turndown(htmlContent)
}

export {
    convertHtmlToMarkdown
}