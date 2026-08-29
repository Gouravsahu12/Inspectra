"""
database.py
------------
Lightweight SQLite storage for users and their generated compliance reports.

SQLite is deliberately chosen here: zero setup, ships as part of Python, and
is more than enough for a hackathon demo / small deployment. If you outgrow
it later, only this file needs to change — everything else talks to it
through the functions below, not raw SQL.
"""

import sqlite3
import json
from contextlib import contextmanager
from datetime import datetime, timezone

DB_PATH = "compliance_app.db"


@contextmanager
def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    try:
        yield conn
        conn.commit()
    finally:
        conn.close()


def init_db():
    """Call this once at app startup to create tables if they don't exist."""
    with get_connection() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
        """)
        conn.execute("""
            CREATE TABLE IF NOT EXISTS reports (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                passed INTEGER NOT NULL,
                result_json TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users (id)
            )
        """)


def create_user(username: str, password_hash: str) -> int:
    with get_connection() as conn:
        cur = conn.execute(
            "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)",
            (username, password_hash, datetime.now(timezone.utc).isoformat()),
        )
        return cur.lastrowid


def get_user_by_username(username: str):
    with get_connection() as conn:
        row = conn.execute("SELECT * FROM users WHERE username = ?", (username,)).fetchone()
        return dict(row) if row else None


def save_report(user_id: int, product_name: str, passed: bool, result_dict: dict) -> int:
    with get_connection() as conn:
        cur = conn.execute(
            """INSERT INTO reports (user_id, product_name, passed, result_json, created_at)
               VALUES (?, ?, ?, ?, ?)""",
            (
                user_id,
                product_name,
                1 if passed else 0,
                json.dumps(result_dict),
                datetime.now(timezone.utc).isoformat(),
            ),
        )
        return cur.lastrowid


def get_reports_for_user(user_id: int):
    with get_connection() as conn:
        rows = conn.execute(
            "SELECT * FROM reports WHERE user_id = ? ORDER BY created_at DESC",
            (user_id,),
        ).fetchall()
        return [dict(r) for r in rows]
