package org.example.project.utils

enum class MessageType {
    FILE_COMMON_DSCR,
    FILE,
    CLIPBOARD_CONTENT,
    CANCEL_TX,
    CANCEL_RX,
    PROGRESS_RX,
    RECEPTION_DONE,
    TX_REQUEST,
    ACCEPT_TX,
    DISMISS_TX,
    PAIR_CREATION_REQUEST,
    ACCEPT_PAIR_CREATION_REQUEST,
    DISMISS_PAIR_CREATION_REQUEST,
    PAIR_CONNECTION_CLOSE,
}

fun isMessageControl(type: MessageType): Boolean {
    return when(type) {
        MessageType.FILE_COMMON_DSCR,
        MessageType.FILE,
        MessageType.CLIPBOARD_CONTENT,
            -> {
            false
        }
        MessageType.CANCEL_TX,
        MessageType.CANCEL_RX,
        MessageType.RECEPTION_DONE,
        MessageType.TX_REQUEST,
        MessageType.ACCEPT_TX,
        MessageType.DISMISS_TX,
        MessageType.PAIR_CREATION_REQUEST,
        MessageType.PROGRESS_RX,
        MessageType.ACCEPT_PAIR_CREATION_REQUEST,
        MessageType.DISMISS_PAIR_CREATION_REQUEST,
        MessageType.PAIR_CONNECTION_CLOSE,
            -> {
            true
        }
    }
}

enum class DataTransferState {
    IDLE,
    ACTIVE,
    DONE,
    READY_TO_RECEIVE,
    READY_TO_TRANSMIT,
    CANCEL_BY_TX,
    CANCEL_BY_RX,
    ERROR
}

enum class DataTransferStatus {
    DONE,
    CANCELED_BY_TX,
    CANCELED_BY_RX,
    ERROR
}
