"""Fetch LeetCode's Daily Challenge and write it to data/daily.json.

The Android widget reads that JSON file. The problem statement is converted
from LeetCode's HTML into a list of simple blocks ({"t": "text"|"code", "v": ...})
so the widget can render it without an HTML engine.
"""
import json
import os
import re
import sys
import urllib.request
from html.parser import HTMLParser

GRAPHQL_URL = "https://leetcode.com/graphql"
QUERY = """
query questionOfToday {
  activeDailyCodingChallengeQuestion {
    date
    link
    question {
      questionFrontendId
      title
      titleSlug
      difficulty
      content
      topicTags { name }
    }
  }
}
"""
OUT_PATH = os.path.join(os.path.dirname(__file__), "..", "data", "daily.json")


class HtmlToBlocks(HTMLParser):
    BLOCK_TAGS = {"p", "div", "ul", "ol", "li", "h1", "h2", "h3", "h4"}

    def __init__(self):
        super().__init__()
        self.blocks = []
        self.buf = []
        self.in_pre = False

    def flush(self):
        raw = "".join(self.buf)
        self.buf = []
        if self.in_pre:
            text = raw.strip("\n")
            kind = "code"
        else:
            text = re.sub(r"[ \t\r\n\xa0]+", " ", raw).strip()
            kind = "text"
        if text:
            self.blocks.append({"t": kind, "v": text})

    def handle_starttag(self, tag, attrs):
        if tag == "pre":
            self.flush()
            self.in_pre = True
        elif tag in self.BLOCK_TAGS:
            self.flush()
            if tag == "li":
                self.buf.append("• ")
        elif tag == "br" and not self.in_pre:
            self.flush()
        elif tag == "sup":
            self.buf.append("^")

    def handle_endtag(self, tag):
        if tag == "pre":
            self.flush()
            self.in_pre = False
        elif tag in self.BLOCK_TAGS:
            self.flush()

    def handle_data(self, data):
        self.buf.append(data)


def html_to_blocks(html):
    parser = HtmlToBlocks()
    parser.feed(html)
    parser.flush()
    return parser.blocks


def fetch():
    body = json.dumps({"query": QUERY}).encode()
    req = urllib.request.Request(
        GRAPHQL_URL,
        data=body,
        headers={
            "Content-Type": "application/json",
            "Referer": "https://leetcode.com/problemset/",
            "User-Agent": "Mozilla/5.0 (daily-dsa widget)",
        },
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        payload = json.load(resp)
    daily = payload["data"]["activeDailyCodingChallengeQuestion"]
    q = daily["question"]
    if not q.get("content"):
        raise RuntimeError("Problem content is empty (paid-only problem?)")
    return {
        "date": daily["date"],
        "id": q["questionFrontendId"],
        "title": q["title"],
        "difficulty": q["difficulty"],
        "link": "https://leetcode.com" + daily["link"],
        "topics": [t["name"] for t in q["topicTags"]],
        "blocks": html_to_blocks(q["content"]),
    }


def main():
    try:
        data = fetch()
    except Exception as exc:  # keep the previous file if LeetCode is unreachable
        print("fetch failed: %s" % exc, file=sys.stderr)
        return 1
    with open(OUT_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print("Wrote %s. %s (%s)" % (data["date"], data["title"], data["difficulty"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
