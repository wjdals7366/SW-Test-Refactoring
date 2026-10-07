import http from 'k6/http';
import { check } from 'k6';

export const options ={
    vus:1,
    iterations: 1,
},

export default function () {
    const url = 'http://localhost:8081/job/launcher';

    const payload = JSON.stringfy({
        name: 'addPassesJob',
        jobParameters: {
            'run_id': String(Date.now())
        }
    });

    const params = {
        header: { 'Content-Type': 'application/json'},
        timeout: '300s',
    };

    const start = Date.now();
    const res = http.post(url, payload, params);
    const elapsed = Date.now() - start;

    console.log('STATUS: ' + res.status);
    console.log('BODY:' + res.body);
    console.log('실제 소요 시간: ' + elapsed + 'ms');

    check(res, {
        'Job 정상 종료 (COMPLETED)': (r) => r.body && r.body.includes('COMPLETED'),
    });
    
}