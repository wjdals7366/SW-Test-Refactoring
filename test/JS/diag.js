import http from 'k6/http';
import encoding from 'k6/encoding';

export const options = {
  vus: 1,
  iterations: 3,
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

  console.log('STATUS: ' + res.status);
  console.log('BODY: ' + res.body);
}