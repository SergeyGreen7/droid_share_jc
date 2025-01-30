package org.example.project.utils

enum class MessageType {
    FILE_COMMON_DSCR,
    FILE,
    CANCEL_TX,
    CANCEL_RX,
    PROGRESS_RX,
    RECEPTION_DONE,
    TX_REQUEST,
    ACCEPT_TX,
    DISMISS_TX,
    TEST_MESSAGE
}

fun isMessageControl(type: MessageType): Boolean {
    return when(type) {
        MessageType.FILE_COMMON_DSCR,
        MessageType.FILE,
        MessageType.PROGRESS_RX -> {
            false
        }
        MessageType.CANCEL_TX,
        MessageType.CANCEL_RX,
        MessageType.RECEPTION_DONE,
        MessageType.TX_REQUEST,
        MessageType.ACCEPT_TX,
        MessageType.DISMISS_TX,
        MessageType.TEST_MESSAGE,-> {
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
