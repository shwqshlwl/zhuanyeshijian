/**
 * 格式化工具函数
 */

/**
 * 去除HTML标签，获取纯文本内容
 * @param {string} html - HTML字符串
 * @param {number} maxLength - 最大长度，默认不限制
 * @returns {string} 纯文本内容
 */
export function stripHtmlTags(html, maxLength = 0) {
  if (!html) return ''

  // 去除HTML标签
  const text = html.replace(/<[^>]+>/g, '')

  // 去除多余的空白字符
  const cleanText = text.replace(/\s+/g, ' ').trim()

  // 如果指定了最大长度，则截取
  if (maxLength > 0 && cleanText.length > maxLength) {
    return cleanText.substring(0, maxLength) + '...'
  }

  return cleanText
}

/**
 * 截取文本摘要
 * @param {string} text - 文本内容
 * @param {number} maxLength - 最大长度
 * @returns {string} 摘要
 */
export function truncateText(text, maxLength = 100) {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}
