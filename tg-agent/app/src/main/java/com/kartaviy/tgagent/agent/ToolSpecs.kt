package com.kartaviy.tgagent.agent

import org.json.JSONArray

/** Описание инструментов для ИИ (формат function calling OpenAI/DeepSeek). */
object ToolSpecs {
    private const val SPECS = """
[
 {"type":"function","function":{"name":"find_chats","description":"Найти чаты/контакты/группы/каналы по названию или имени. Возвращает chat_id. Вызывай перед любым действием с чатом, названным по имени.",
  "parameters":{"type":"object","properties":{
   "query":{"type":"string","description":"Имя, название или @username"},
   "limit":{"type":"integer","description":"Сколько вернуть, по умолчанию 10"}},"required":["query"]}}},

 {"type":"function","function":{"name":"list_chats","description":"Список чатов: основной список, архив или папка (по порядку, как в Telegram).",
  "parameters":{"type":"object","properties":{
   "folder_id":{"type":"integer","description":"ID папки; если не указан — основной список"},
   "archive":{"type":"boolean","description":"true — показать архив"},
   "limit":{"type":"integer","description":"По умолчанию 50"}}}}},

 {"type":"function","function":{"name":"search_messages","description":"Поиск сообщений по сложным условиям. Условия объединяются через И. Где искать: chat_ids, или folder_id, или all_chats. Найденные сообщения пользователь видит списком в приложении.",
  "parameters":{"type":"object","properties":{
   "chat_ids":{"type":"array","items":{"type":"integer"},"description":"В каких чатах искать"},
   "folder_id":{"type":"integer","description":"Искать во всех чатах папки"},
   "all_chats":{"type":"boolean","description":"Искать во всех чатах"},
   "chat_type":{"type":"string","enum":["private","group","channel"],"description":"Только для all_chats: тип чатов"},
   "text":{"type":"string","description":"Слово или фраза в тексте/подписи"},
   "forwarded_only":{"type":"boolean","description":"Только пересланные сообщения"},
   "forwarded_from":{"type":"string","description":"Пересланные от этого человека/канала (часть имени)"},
   "sender":{"type":"string","description":"Кто отправил: часть имени, или 'me' — мои сообщения"},
   "content_type":{"type":"string","enum":["text","photo","video","document","voice","audio","gif","sticker","video_note","link"]},
   "date_from":{"type":"string","description":"YYYY-MM-DD или YYYY-MM-DDTHH:MM"},
   "date_to":{"type":"string","description":"YYYY-MM-DD или YYYY-MM-DDTHH:MM"},
   "limit":{"type":"integer","description":"Максимум результатов, по умолчанию 200"},
   "scan_limit":{"type":"integer","description":"Сколько сообщений максимум просмотреть, по умолчанию 5000"}}}}},

 {"type":"function","function":{"name":"send_message","description":"Отправить сообщение сейчас (пользователь подтвердит).",
  "parameters":{"type":"object","properties":{
   "chat_id":{"type":"integer"},"text":{"type":"string"}},"required":["chat_id","text"]}}},

 {"type":"function","function":{"name":"schedule_message","description":"Запланировать отложенное сообщение (пользователь подтвердит). Отправит сам Telegram, даже если телефон выключен.",
  "parameters":{"type":"object","properties":{
   "chat_id":{"type":"integer"},"text":{"type":"string"},
   "send_at":{"type":"string","description":"Местное время YYYY-MM-DDTHH:MM"}},"required":["chat_id","text","send_at"]}}},

 {"type":"function","function":{"name":"list_scheduled","description":"Показать запланированные сообщения в чате или во всех чатах.",
  "parameters":{"type":"object","properties":{
   "chat_id":{"type":"integer","description":"Если не указан — по всем чатам"}}}}},

 {"type":"function","function":{"name":"reschedule_message","description":"Перенести запланированное сообщение на другое время (пользователь подтвердит).",
  "parameters":{"type":"object","properties":{
   "chat_id":{"type":"integer"},"message_id":{"type":"integer"},
   "send_at":{"type":"string","description":"YYYY-MM-DDTHH:MM"}},"required":["chat_id","message_id","send_at"]}}},

 {"type":"function","function":{"name":"cancel_scheduled","description":"Удалить запланированные сообщения (пользователь подтвердит).",
  "parameters":{"type":"object","properties":{
   "chat_id":{"type":"integer"},"message_ids":{"type":"array","items":{"type":"integer"}}},"required":["chat_id","message_ids"]}}},

 {"type":"function","function":{"name":"list_folders","description":"Список папок Telegram с их ID.",
  "parameters":{"type":"object","properties":{}}}},

 {"type":"function","function":{"name":"folder_chats","description":"Какие чаты сейчас в папке.",
  "parameters":{"type":"object","properties":{"folder_id":{"type":"integer"}},"required":["folder_id"]}}},

 {"type":"function","function":{"name":"create_folder","description":"Создать папку и положить в неё чаты (пользователь подтвердит). Название до 12 символов.",
  "parameters":{"type":"object","properties":{
   "name":{"type":"string"},"chat_ids":{"type":"array","items":{"type":"integer"}}},"required":["name","chat_ids"]}}},

 {"type":"function","function":{"name":"add_to_folder","description":"Добавить чаты в существующую папку (пользователь подтвердит).",
  "parameters":{"type":"object","properties":{
   "folder_id":{"type":"integer"},"chat_ids":{"type":"array","items":{"type":"integer"}}},"required":["folder_id","chat_ids"]}}},

 {"type":"function","function":{"name":"remove_from_folder","description":"Убрать чаты из папки (пользователь подтвердит).",
  "parameters":{"type":"object","properties":{
   "folder_id":{"type":"integer"},"chat_ids":{"type":"array","items":{"type":"integer"}}},"required":["folder_id","chat_ids"]}}},

 {"type":"function","function":{"name":"rename_folder","description":"Переименовать папку (пользователь подтвердит). До 12 символов.",
  "parameters":{"type":"object","properties":{
   "folder_id":{"type":"integer"},"name":{"type":"string"}},"required":["folder_id","name"]}}},

 {"type":"function","function":{"name":"delete_folder","description":"Удалить папку; сами чаты не удаляются (пользователь подтвердит).",
  "parameters":{"type":"object","properties":{"folder_id":{"type":"integer"}},"required":["folder_id"]}}}
]
"""

    val all: JSONArray get() = JSONArray(SPECS)
}
