const tradeDetailChatBtn =
    document.getElementById('tradeDetailChatBtn');

const tradeChatBtn =
    document.getElementById('tradeChatBtn');


const tradeChatRoomModal =
    document.getElementById('tradeChatRoomModal');

const tradeChatRoomBackdrop =
    document.getElementById('tradeChatRoomBackdrop');

const tradeChatRoomCloseBtn =
    document.getElementById('tradeChatRoomCloseBtn');

const tradeChatRoomList =
    document.getElementById('tradeChatRoomList');

const tradeChatRoomEmpty =
    document.getElementById('tradeChatRoomEmpty');


const tradeMessageSelectEmpty =
    document.getElementById('tradeMessageSelectEmpty');

const tradeMessageModal =
    document.getElementById('tradeMessageModal');

const tradeMessageTitle =
    document.getElementById('tradeMessageTitle');

const tradeMessageOpponent =
    document.getElementById('tradeMessageOpponent');

const tradeMessageProductTitle =
    document.getElementById('tradeMessageProductTitle');

const tradeMessageCloseBtn =
    document.getElementById('tradeMessageCloseBtn');

const tradeMessageList =
    document.getElementById('tradeMessageList');

const tradeMessageEmpty =
    document.getElementById('tradeMessageEmpty');

const tradeMessageForm =
    document.getElementById('tradeMessageForm');

const tradeMessageInput =
    document.getElementById('tradeMessageInput');


let currentMessageRoomId =
    null;

let currentTradeMessageRoom =
    null;

let tradeChatRooms =
    [];

let tradeChatEventSource =
    null;


/* =====================================================
   날짜
   ===================================================== */

function formatTradeChatDate(createdAt) {

    if (!createdAt) {
        return '';
    }


    return createdAt
        .replace('T', ' ')
        .substring(5, 16);
}


/* =====================================================
   SSE 연결
   ===================================================== */

function connectTradeChatSse(messageRoomId) {

    closeTradeChatSse();


    tradeChatEventSource =
        new EventSource(
            CTX
            + 'sse/connect?messageRoomId='
            + encodeURIComponent(messageRoomId)
        );


    tradeChatEventSource.addEventListener(
        'connect',
        function () {

            console.log(
                '[TRADE SSE] 연결 완료 messageRoomId :',
                messageRoomId
            );
        }
    );


    tradeChatEventSource.addEventListener(
        'message',
        function () {

            if (
                !currentMessageRoomId
                || String(currentMessageRoomId) !== String(messageRoomId)
            ) {
                return;
            }


            console.log(
                '[TRADE SSE] 새 메시지 messageRoomId :',
                messageRoomId
            );


            loadTradeMessageList(
                true
            );


            loadTradeChatRoomList();
        }
    );


    tradeChatEventSource.addEventListener(
        'read',
        function () {

            if (
                !currentMessageRoomId
                || String(currentMessageRoomId) !== String(messageRoomId)
            ) {
                return;
            }


            console.log(
                '[TRADE SSE] 읽음 처리 messageRoomId :',
                messageRoomId
            );


            loadTradeMessageList(
                false
            );


            loadTradeChatRoomList();
        }
    );


    tradeChatEventSource.onerror =
        function () {

            console.warn(
                '[TRADE SSE] 연결 오류 messageRoomId :',
                messageRoomId
            );
        };
}


function closeTradeChatSse() {

    if (!tradeChatEventSource) {
        return;
    }


    tradeChatEventSource.close();

    tradeChatEventSource =
        null;


    console.log(
        '[TRADE SSE] 연결 종료'
    );
}


/* =====================================================
   채팅 전체 모달
   ===================================================== */

function openTradeChatRoomModal() {

    tradeChatRoomModal.hidden =
        false;
}


function closeTradeChatRoomModal() {

    closeTradeChatSse();


    tradeChatRoomModal.hidden =
        true;

    currentMessageRoomId =
        null;

    currentTradeMessageRoom =
        null;

    tradeChatRooms =
        [];

    tradeChatRoomList.innerHTML =
        '';

    closeTradeMessagePanel();
}


/* =====================================================
   메시지 영역
   ===================================================== */

function openTradeMessagePanel(room) {

    currentMessageRoomId =
        room.messageRoomId;

    currentTradeMessageRoom =
        room;


    tradeMessageSelectEmpty.hidden =
        true;

    tradeMessageModal.hidden =
        false;


    tradeMessageTitle.textContent =
        '거래 채팅';

    tradeMessageOpponent.textContent =
        room.opponentName;

    tradeMessageProductTitle.textContent =
        room.tradePostTitle;


    updateTradeChatRoomActive();


    connectTradeChatSse(
        room.messageRoomId
    );


    loadTradeMessageList(
        true
    );


    tradeMessageInput.focus();
}


function closeTradeMessagePanel() {

    closeTradeChatSse();


    currentMessageRoomId =
        null;

    currentTradeMessageRoom =
        null;


    tradeMessageModal.hidden =
        true;

    tradeMessageSelectEmpty.hidden =
        false;


    tradeMessageList.innerHTML =
        '';

    tradeMessageList.hidden =
        false;

    tradeMessageEmpty.hidden =
        true;

    tradeMessageInput.value =
        '';


    updateTradeChatRoomActive();
}


/* =====================================================
   채팅방 선택 상태
   ===================================================== */

function updateTradeChatRoomActive() {

    const roomItems =
        tradeChatRoomList.querySelectorAll(
            '.trade-chat-room-item'
        );


    roomItems.forEach(function (item) {

        if (
            currentMessageRoomId
            && String(item.dataset.id) === String(currentMessageRoomId)
        ) {

            item.classList.add(
                'active'
            );

        } else {

            item.classList.remove(
                'active'
            );
        }
    });
}


/* =====================================================
   채팅방 목록 렌더링
   ===================================================== */

function renderTradeChatRoomList(list) {

    tradeChatRoomList.innerHTML =
        '';


    tradeChatRooms =
        Array.isArray(list)
            ? list
            : [];


    if (tradeChatRooms.length === 0) {

        tradeChatRoomList.hidden =
            true;

        tradeChatRoomEmpty.hidden =
            false;

        return;
    }


    tradeChatRoomList.hidden =
        false;

    tradeChatRoomEmpty.hidden =
        true;


    tradeChatRooms.forEach(function (item) {

        const roomItem =
            document.createElement('button');


        roomItem.type =
            'button';

        roomItem.className =
            'trade-chat-room-item';

        roomItem.dataset.id =
            item.messageRoomId;


        const top =
            document.createElement('div');


        top.className =
            'trade-chat-room-item-top';


        const opponent =
            document.createElement('strong');


        opponent.className =
            'trade-chat-room-opponent';

        opponent.textContent =
            item.opponentName;


        const time =
            document.createElement('span');


        time.className =
            'trade-chat-room-time';

        time.textContent =
            formatTradeChatDate(
                item.lastMessageAt
            );


        top.appendChild(
            opponent
        );

        top.appendChild(
            time
        );


        const product =
            document.createElement('span');


        product.className =
            'trade-chat-room-product';

        product.textContent =
            item.tradePostTitle;


        const bottom =
            document.createElement('div');


        bottom.className =
            'trade-chat-room-bottom';


        const lastMessage =
            document.createElement('span');


        lastMessage.className =
            'trade-chat-room-last-message';

        lastMessage.textContent =
            item.lastMessageText
                ? item.lastMessageText
                : '아직 대화 내용이 없습니다.';


        bottom.appendChild(
            lastMessage
        );


        if (Number(item.unreadCount) > 0) {

            const unread =
                document.createElement('span');


            unread.className =
                'trade-chat-room-unread';

            unread.textContent =
                item.unreadCount;


            bottom.appendChild(
                unread
            );
        }


        roomItem.appendChild(
            top
        );

        roomItem.appendChild(
            product
        );

        roomItem.appendChild(
            bottom
        );


        tradeChatRoomList.appendChild(
            roomItem
        );
    });


    updateTradeChatRoomActive();
}


/* =====================================================
   채팅방 목록 조회
   ===================================================== */

function loadTradeChatRoomList(callback) {

    ajaxGet(
        CTX + 'trade/chat/rooms',
        function (list) {

            renderTradeChatRoomList(
                list
            );


            if (callback) {

                callback(
                    tradeChatRooms
                );
            }
        }
    );
}


/* =====================================================
   메시지 렌더링
   ===================================================== */

function renderTradeMessageList(list) {

    tradeMessageList.innerHTML =
        '';


    if (!Array.isArray(list) || list.length === 0) {

        tradeMessageList.hidden =
            true;

        tradeMessageEmpty.hidden =
            false;

        return;
    }


    tradeMessageList.hidden =
        false;

    tradeMessageEmpty.hidden =
        true;


    list.forEach(function (item) {

        const isMine =
            currentTradeMessageRoom
            && String(item.senderUserId)
            !== String(currentTradeMessageRoom.opponentUserId);


        const messageItem =
            document.createElement('div');


        messageItem.className =
            'trade-message-item '
            + (
                isMine
                    ? 'mine'
                    : 'opponent'
            );


        if (!isMine) {

            const sender =
                document.createElement('span');


            sender.className =
                'trade-message-sender';

            sender.textContent =
                currentTradeMessageRoom.opponentName;


            messageItem.appendChild(
                sender
            );
        }


        const content =
            document.createElement('div');


        content.className =
            'trade-message-content';

        content.textContent =
            item.messageText;


        const meta =
            document.createElement('div');


        meta.className =
            'trade-message-meta';


        if (isMine && !item.readAt) {

            const unread =
                document.createElement('span');


            unread.className =
                'trade-message-unread';

            unread.textContent =
                '1';


            meta.appendChild(
                unread
            );
        }


        const date =
            document.createElement('span');


        date.textContent =
            formatTradeChatDate(
                item.createdAt
            );


        meta.appendChild(
            date
        );


        messageItem.appendChild(
            content
        );

        messageItem.appendChild(
            meta
        );


        tradeMessageList.appendChild(
            messageItem
        );
    });


    tradeMessageList.scrollTop =
        tradeMessageList.scrollHeight;
}


/* =====================================================
   메시지 목록 조회
   ===================================================== */

function loadTradeMessageList(readCheck) {

    if (!currentMessageRoomId) {
        return;
    }


    const messageRoomId =
        currentMessageRoomId;


    ajaxGet(
        CTX
        + 'trade/chat/messages?messageRoomId='
        + encodeURIComponent(messageRoomId),
        function (list) {

            if (
                !currentMessageRoomId
                || String(currentMessageRoomId) !== String(messageRoomId)
            ) {
                return;
            }


            renderTradeMessageList(
                list
            );


            if (readCheck === false) {
                return;
            }


            readTradeMessage(
                messageRoomId
            );
        }
    );
}


/* =====================================================
   메시지 읽음 처리
   ===================================================== */

function readTradeMessage(messageRoomId) {

    ajaxPost(
        CTX + 'trade/chat/read',
        {
            messageRoomId: messageRoomId
        },
        function (res) {

            if (
                !currentMessageRoomId
                || String(currentMessageRoomId) !== String(messageRoomId)
            ) {
                return;
            }


            if (Number(res) > 0) {

                loadTradeChatRoomList();
            }
        }
    );
}


/* =====================================================
   거래 상세에서 채팅 시작
   ===================================================== */

function openTradeChat(tradePostId) {

    ajaxPost(
        CTX + 'trade/chat/room',
        {
            tradePostId: tradePostId
        },
        function (room) {

            if (!room || !room.messageRoomId) {

                openAlert(
                    '거래 채팅',
                    '채팅방 정보를 불러오지 못했습니다.'
                );

                return;
            }


            closeTradeDetailModal();

            openTradeChatRoomModal();


            loadTradeChatRoomList(
                function (list) {

                    const targetRoom =
                        list.find(function (item) {

                            return String(item.messageRoomId)
                                === String(room.messageRoomId);
                        });


                    if (!targetRoom) {

                        openAlert(
                            '거래 채팅',
                            '채팅방 정보를 찾을 수 없습니다.'
                        );

                        return;
                    }


                    openTradeMessagePanel(
                        targetRoom
                    );
                }
            );
        }
    );
}


/* =====================================================
   하단 채팅 버튼
   ===================================================== */

tradeChatBtn.addEventListener(
    'click',
    function () {

        openTradeChatRoomModal();

        closeTradeMessagePanel();


        loadTradeChatRoomList();
    }
);


/* =====================================================
   거래 상세 판매자와 채팅
   ===================================================== */

tradeDetailChatBtn.addEventListener(
    'click',
    function () {

        const tradePostId =
            document.getElementById(
                'tradeDetailFavoriteBtn'
            ).dataset.id;


        if (!tradePostId) {

            console.warn(
                '[TRADE CHAT] tradePostId가 없습니다.'
            );


            openAlert(
                '거래 채팅',
                '거래글 정보를 찾을 수 없습니다.'
            );

            return;
        }


        console.log(
            '[TRADE CHAT] 채팅 시작 tradePostId :',
            tradePostId
        );


        openTradeChat(
            tradePostId
        );
    }
);


/* =====================================================
   채팅방 목록 선택
   ===================================================== */

tradeChatRoomList.addEventListener(
    'click',
    function (e) {

        const roomItem =
            e.target.closest(
                '.trade-chat-room-item'
            );


        if (!roomItem) {
            return;
        }


        const messageRoomId =
            roomItem.dataset.id;


        const room =
            tradeChatRooms.find(function (item) {

                return String(item.messageRoomId)
                    === String(messageRoomId);
            });


        if (!room) {
            return;
        }


        openTradeMessagePanel(
            room
        );
    }
);


/* =====================================================
   메시지 전송
   ===================================================== */

tradeMessageForm.addEventListener(
    'submit',
    function (e) {

        e.preventDefault();


        if (!currentMessageRoomId) {

            console.warn(
                '[TRADE CHAT] messageRoomId가 없습니다.'
            );

            return;
        }


        const messageText =
            tradeMessageInput.value.trim();


        if (!messageText) {
            return;
        }


        const messageRoomId =
            currentMessageRoomId;


        ajaxPost(
            CTX + 'trade/chat/send',
            {
                messageRoomId: messageRoomId,
                messageText: messageText
            },
            function () {

                if (
                    !currentMessageRoomId
                    || String(currentMessageRoomId) !== String(messageRoomId)
                ) {
                    return;
                }


                tradeMessageInput.value =
                    '';

                tradeMessageInput.focus();


                loadTradeMessageList(
                    false
                );


                loadTradeChatRoomList();
            }
        );
    }
);


/* =====================================================
   메시지 영역 닫기
   ===================================================== */

tradeMessageCloseBtn.addEventListener(
    'click',
    function () {

        closeTradeMessagePanel();
    }
);


/* =====================================================
   전체 채팅 닫기
   ===================================================== */

tradeChatRoomCloseBtn.addEventListener(
    'click',
    function () {

        closeTradeChatRoomModal();
    }
);


tradeChatRoomBackdrop.addEventListener(
    'click',
    function () {

        closeTradeChatRoomModal();
    }
);