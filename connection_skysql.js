const mariadb = require('mariadb'); // MariaDB 모듈을 가져옵니다.

// Node 파일 시스템 패키지에 액세스
const fs = require("fs");

async function asyncFunction() {
    let conn;
    try {
        // 인증 기관 체인 파일을 읽어옵니다.(데이터 안전하게 전송하기 위한 것)
        const serverCert = [fs.readFileSync("path/to/skysql_chain.pem", "utf8")];

        // MariaDB에 새로운 연결을 만듭니다.
        conn = await mariadb.createConnection({
            host: '<호스트 주소 입력>', // MariaDB 호스트 주소 입력
            port: '<포트 번호 입력>', // MariaDB 포트 번호 입력
            user: '<사용자 입력>', // MariaDB 사용자 이름 입력
            password: '<비밀번호 입력>', // MariaDB 비밀번호 입력

            // 연결 풀 설정에 "ssl" 속성을 추가하고 위에서 정의한 serverCert 변수를 사용하여 SSL 설정을 구성합니다.
            ssl: {
                ca: serverCert
            }
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