#!/usr/bin/env python3
"""Minimal mock of the LTA DataMall BusArrivalv2 API for local smoke tests."""
import json
from http.server import BaseHTTPRequestHandler, HTTPServer

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
        if "BusStopCode=83139" not in self.path:
            self.send_response(404)
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
