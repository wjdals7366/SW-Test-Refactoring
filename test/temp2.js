import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';

export const options = {
  scenarios: {
    bulk_pass_insert: {
      executor: 'shared-iterations',
      vus: 20,
      iterations: 10000,
      maxDuration: '10m',
    },
  },
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
    redirects: 0,   // ← 추가: 302를 자동으로 따라가지 않음
  };

  const res = http.post(url, payload, params);

  check(res, {
    '등록 성공 (302)': (r) => r.status === 302,
  });
}