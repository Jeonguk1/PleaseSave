const mariadb = require('mariadb');

async function asyncFunction() {
    let conn;
    try {
        // 새로운 연결을 만듭니다.
        conn = await mariadb.createConnection({
            host: 'localhost',
            port: '3306',
            user: 'savedb',
            password: '12345',
        });

        // 연결 스레드 ID를 출력합니다.
        console.log(`연결 성공! (id=${conn.threadId})`);
    } catch (err) {
        // 오류 메시지를 출력합니다.
        console.log(err);
    } finally {
        // 연결을 닫습니다.
        if (conn) await conn.close();
    }
}

// asyncFunction 함수를 호출하여 코드를 실행합니다.
asyncFunction();
