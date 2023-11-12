const express = require('express');
const app = express();
const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt'); // bcrypt 모듈 추가
app.use(express.json());
const crypto = require('crypto');

const hostname = '0.0.0.0'; // 변경하려는 호스트 주소
const port = 3306;

const pool = mysql.createPool({
    host: 'database-1.cvsgdmkbd0ia.ap-northeast-2.rds.amazonaws.com',
    user: 'admin',
    password: '12345678',
    database: 'savedb',
    connectionLimit: 10, // 필요에 따라 조절
    charset: 'utf8mb4'
});

const server = require("http").createServer(app);
const cors = require("cors");
app.use(cors());



server.listen(port, hostname, () => {
  console.log(`서버가 http://${hostname}:${port}/ 에서 실행 중입니다.`);
});

server.listen(port, () => {
    console.log(port + ' 포트에서 서버가 실행 중입니다.');
});

app.get("/", function (req, res) {
    console.log("main");
    res.send('Welcome!');
});

app.get("/test", function (req, res) {
    console.log("hello");
    res.send('test log');
});

app.get('/users', async (req, res) => {
    const { email, password } = req.query;
  
    try {
      const [rows] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
  
      if (rows.length === 1) {
        const hashedPassword = rows[0].password;
        
        // 입력한 비밀번호를 SHA-256 해싱
        const inputPasswordHash = crypto.createHash('sha256').update(password).digest('hex');
  
        if (inputPasswordHash === hashedPassword) {
          console.log("로그인 성공");
          res.json({ result: 0, message: '로그인 성공' });
        } else {
          console.log("비밀번호가 일치하지 않음");
          res.json({ result: 1, message: '비밀번호가 일치하지 않음' });
        }
      } else {
        console.log("사용자를 찾을수 없습니다");
        res.json({ result: 1, message: '사용자를 찾을수 없습니다' });
      }
    } catch (err) {
      console.error('데이터베이스 쿼리 오류:', err);
      res.status(500).json({ result: 2, message: '내부 서버 오류' });
    }
  });



app.get('/signup1', async (req, res) => {
    const { email } = req.query;
  
    try {
        const [rows] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
  
        if (rows.length > 0) {
            console.log("이미 사용중인 이메일 존재");
            res.json({ result: 1, message: '이미 사용중인 이메일 존재' }); 
        } else {
            console.log("pass");
            res.json({ result: 0, message: 'pass' }); 
        }
    } catch (err) {
        console.error('데이터베이스 쿼리 오류:', err);
        res.status(500).json({ result: 2, message: '내부 서버 오류' });
    }
  });

  app.get('/signup2', async (req, res) => {
    const { username } = req.query;
  
    try {
        const [rows] = await pool.query('SELECT * FROM users WHERE username = ?', [username]);
  
        if (rows.length > 0) {
            console.log("동일한 사용자 이름 존재");
            res.json({ result: 1, message: '동일한 사용자 이름 존재' }); 
        } else {
            console.log("성공");
            res.json({ result: 0, message: '성공' }); 
        }
    } catch (err) {
        console.error('데이터베이스 쿼리 오류:', err);
        res.status(500).json({ result: 2, message: '내부 서버 오류' });
    }
  });

  app.post('/signup3', async (req, res) => {
    const { email, password, phone_number, name, username } = req.body;

    try {
        // 비밀번호를 SHA-256 해싱
        const hashedPassword = crypto.createHash('sha256').update(password).digest('hex');

        const [result] = await pool.query('INSERT INTO users (email, password, phone_number, name, username) VALUES (?, ?, ?, ?, ?)', [email, hashedPassword, phone_number, name, username]);

        if (result.affectedRows > 0) {
            console.log("사용자 등록 성공");
            res.json({ result: 0, message: '사용자 등록 성공' });
        } else {
            console.log("사용자 등록 실패");
            res.json({ result: 1, message: '사용자 등록 실패' });
        }
    } catch (err) {
        console.error('데이터베이스 쿼리 오류:', err);
        res.status(500).json({ result: 2, message: '내부 서버 오류' });
    }
});

app.post('/signup4', async (req, res) => {
    const { email, password, phone_number, name, username } = req.body;

    try {
        // 비밀번호를 SHA-256 해싱
        const hashedPassword = crypto.createHash('sha256').update(password).digest('hex');

        const [result] = await pool.query('INSERT INTO users (email, password, phone_number, name, username) VALUES (?, ?, ?, ?, ?)', [email, hashedPassword, phone_number, name, username]);

        if (result.affectedRows > 0) {
            console.log("사용자 등록 성공");
            res.json({ result: 0, message: '사용자 등록 성공' });
        } else {
            console.log("사용자 등록 실패");
            res.json({ result: 1, message: '사용자 등록 실패' });
        }
    } catch (err) {
        console.error('데이터베이스 쿼리 오류:', err);
        res.status(500).json({ result: 2, message: '내부 서버 오류' });
    }
});


app.post('/signup', async (req, res) => {
    const { email, password, name, phoneNumber, username } = req.body;

    try {
        const [existingEmail] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);

        if (existingEmail.length > 0) {
            console.log("이메일 이미 존재");
            res.json({ result: 1, message: '이메일 이미 존재' });
        } else {
            const [existingUsername] = await pool.query('SELECT * FROM users WHERE username = ?', [username]);

            if (existingUsername.length > 0) {
                console.log("사용자 이름 이미 존재");
                res.json({ result: 2, message: '사용자 이름 이미 존재' });
            } else {
                // 비밀번호를 SHA-256 해싱
                const hashedPassword = crypto.createHash('sha256').update(password).digest('hex');

                const [result] = await pool.query('INSERT INTO users (email, password, phone_number, name, username) VALUES (?, ?, ?, ?, ?)', [email, hashedPassword, phoneNumber, name, username]);

                if (result.affectedRows > 0) {
                    console.log("사용자 등록 성공");
                    res.json({ result: 0, message: '사용자 등록 성공' });
                } else {
                    console.log("사용자 등록 실패");
                    res.json({ result: 3, message: '사용자 등록 실패' });
                }
            }
        }
    } catch (err) {
        console.error('데이터베이스 쿼리 오류:', err);
        res.status(500).json({ result: 4, message: 'Internal Server Error' });
    }
});