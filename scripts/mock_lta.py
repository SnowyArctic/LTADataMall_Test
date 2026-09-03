#!/usr/bin/env python3
"""Minimal mock of the LTA DataMall Bus Arrival API (v3) for local smoke tests.

Mirrors real LTA behavior:
- /ltaodataservice/v3/BusArrival serves the payload when AccountKey is valid
- missing AccountKey -> 404, invalid AccountKey -> 401
"""
import json
from http.server import BaseHTTPRequestHandler, HTTPServer

VALID_KEY = "TESTKEY"

PAYLOAD = {
    "Services": [
        {"ServiceNo": "964",
         "NextBus": {"EstimatedArrival": "2026-08-31T10:05:00+08:00", "Load": "SEA"},
         "NextBus2": {"EstimatedArrival": "2026-08-31T10:12:00+08:00", "Load": "SDA"},
         "NextBus3": {}},
        {"ServiceNo": "117",
         "NextBus": {"EstimatedArrival": "2026-08-31T10:02:00+08:00", "Load": "LSD"},
         "NextBus2": {}, "NextBus3": {}},
    ]
}


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        key = self.headers.get("AccountKey", "")
        if "/ltaodataservice/v3/BusArrival" not in self.path or "BusStopCode=83139" not in self.path:
            self.send_response(404)
            self.end_headers()
            return
        if not key:
            self.send_response(404)  # LTA returns 404 for a missing key
            self.end_headers()
            return
        if key != VALID_KEY:
            self.send_response(401)  # LTA returns 401 for an invalid key
            self.end_headers()
            return
        body = json.dumps(PAYLOAD).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, *args):
        pass


if __name__ == "__main__":
    HTTPServer(("127.0.0.1", 9911), Handler).serve_forever()