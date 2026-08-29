"""
auth.py
--------
Signup / login logic. Passwords are hashed with bcrypt — never stored or
compared in plaintext.

Usage from your Streamlit app:

    from backend import auth, database
    database.init_db()

    ok, msg = auth.signup("lavender", "somepassword123")
    ok, user = auth.login("lavender", "somepassword123")
"""

import re
import bcrypt
from backend import database

USERNAME_RE = re.compile(r"^[a-zA-Z0-9_]{3,30}$")


def _hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt()).decode("utf-8")


def _verify_password(password: str, password_hash: str) -> bool:
    return bcrypt.checkpw(password.encode("utf-8"), password_hash.encode("utf-8"))


def signup(username: str, password: str):
    """Returns (success: bool, message: str)."""
    username = username.strip()
    if not USERNAME_RE.match(username):
        return False, "Username must be 3-30 characters: letters, numbers, underscore only."
    if len(password) < 8:
        return False, "Password must be at least 8 characters."
    if database.get_user_by_username(username):
        return False, "That username is already taken."

    database.create_user(username, _hash_password(password))
    return True, "Account created — you can log in now."


def login(username: str, password: str):
    """Returns (success: bool, user_dict_or_message)."""
    user = database.get_user_by_username(username.strip())
    if not user:
        return False, "No account with that username."
    if not _verify_password(password, user["password_hash"]):
        return False, "Incorrect password."
    return True, user
