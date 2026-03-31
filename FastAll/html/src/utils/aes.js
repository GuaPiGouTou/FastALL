import CryptoJS from 'crypto-js'

/**
 * AES 加密 (ECB 模式, PKCS7 填充)
 * @param {String} word 待加密字符串
 * @param {String} keyStr 密钥
 */
export function aesEncrypt(word, keyStr) {
  const key = CryptoJS.enc.Utf8.parse(keyStr)
  const srcs = CryptoJS.enc.Utf8.parse(word)
  const encrypted = CryptoJS.AES.encrypt(srcs, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  })
  return encrypted.toString()
}
