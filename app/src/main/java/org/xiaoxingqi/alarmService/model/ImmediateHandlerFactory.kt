package org.xiaoxingqi.alarmService.model

import org.xiaoxingqi.alarmService.statemachine.HandlerFactory
import org.xiaoxingqi.alarmService.statemachine.IHandler
import org.xiaoxingqi.alarmService.statemachine.Message
import org.xiaoxingqi.alarmService.statemachine.MessageHandler


class ImmediateHandlerFactory : HandlerFactory {
    override fun create(messageHandler: MessageHandler): IHandler {
        return object : IHandler {
            val messages = mutableListOf<Message>()

            override fun sendMessageAtFrontOfQueue(message: Message) {
                sendMessage(message, true)
            }

            override fun sendMessage(message: Message) {
                sendMessage(message, false)
            }

            private fun sendMessage(message: Message, front: Boolean) {
                when {
                    messages.isEmpty() -> {
                        messageHandler.handleMessage(message)
                    }
                    front -> {
                        messages.add(0, message)
                    }
                    else -> {
                        messages.add(message)
                    }
                }
                while (messages.isNotEmpty()) {
                    messageHandler.handleMessage(messages.removeAt(0))
                }
            }

            override fun obtainMessage(what: Int, obj: Any): Message {
                return Message(what, this, null, null, obj)
            }

            override fun obtainMessage(what: Int): Message {
                return Message(what, this, null, null, null)
            }
        }
    }
}