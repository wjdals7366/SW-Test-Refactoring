import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';

export const options = {
  vus: 100,            // 우선 5명으로 낮춰서 테스트
  iterations: 100,
};

const credentials = encoding.b64encode('admin:admin123');

export default function () {
  const url = 'http://localhost:8080/admin/bulk-pass';
  const payload = 'packageSeq=1&userId=A1000000&startedAt=2026-09-21 10:00';

  const params = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic ' + credentials,
    },
  };

  const res = http.post(url, payload, params);
  const ok = res.status === 200 || res.status === 302;

  check(res, { '성공': () => ok });

  if (!ok) {
    console.log('실패! STATUS: ' + res.status + ' / BODY 앞부분: ' + res.body.substring(0, 200));
  }
}