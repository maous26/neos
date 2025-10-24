# --- script abrégé/robuste (version que je t’ai donnée) ---
# lit M3U (même .gz), nettoie, résume et écrit summary.json
import argparse, re, json, hashlib, gzip
from urllib.parse import urlparse, urlunparse
from pathlib import Path

def sha(h): import hashlib; return hashlib.sha256(h.encode('utf-8')).hexdigest()[:10]
def clean_url(u):
    try:
        p = urlparse(u.strip()); host = (p.hostname or '')
        return urlunparse(p._replace(netloc=(sha(host)+(f":{p.port}" if p.port else '')),
                                     params='', query='', fragment=''))
    except: return ""

def read_text_maybe_gzip(path: str) -> str:
    b = Path(path).read_bytes()
    return gzip.decompress(b).decode("utf-8","ignore") if b[:2]==b"\x1f\x8b" else b.decode("utf-8-sig","ignore")

def detect_quality(t: str):
    t=t.lower()
    return {"uhd": any(k in t for k in ["4k","uhd"]),
            "fhd": "1080" in t or "fhd" in t,
            "hd": "720" in t or " hd" in t or t.endswith(" hd"),
            "hevc": any(k in t for k in ["hevc","h265","h.265"]),
            "fps50": "50fps" in t or "50 fps" in t,
            "fps60": "60fps" in t or "60 fps" in t}

def parse_m3u(path_or_str: str):
    if Path(path_or_str).exists():
        txt = read_text_maybe_gzip(path_or_str)
    else:
        txt = path_or_str
    lines = [l.rstrip("\r\n") for l in txt.splitlines()]
    entries = []; pending=None; current_group=None
    for line in lines:
        if not line or line.startswith("#EXTM3U"): continue
        if line.startswith("#EXTGRP:"):
            current_group = line.split(":",1)[1].strip() or current_group; continue
        if line.startswith("#EXTINF"):
            import re as _re
            attrs = dict(_re.findall(r'([a-zA-Z0-9\-]+)="([^"]*)"', line))
            title = line.split(",",1)[1].strip() if "," in line else attrs.get("tvg-name") or "Unknown"
            pending = {"title": title, "group": attrs.get("group-title") or current_group,
                       "epg_id": attrs.get("tvg-id"), "logo_present": bool(attrs.get("tvg-logo")),
                       "meta": detect_quality(f"{title} {current_group or ''}")}; continue
        if line.startswith("#"): continue
        url=line.strip()
        if not (url.startswith("http://") or url.startswith("https://")): continue
        clean=clean_url(url)
        if pending:
            pending["url"]=clean; entries.append(pending); pending=None
        else:
            entries.append({"title":"Unknown","group":current_group,"epg_id":None,"logo_present":False,
                            "meta":detect_quality(current_group or ""), "url":clean})
    return entries

def summarize(entries, keep_names=False, sample_name_max=20):
    from collections import Counter
    country_rx = re.compile(r"\b([A-Z]{2})\b"); country_counts=Counter(); group_counts=Counter(); qual=Counter()
    has_epg=0; logos=0
    for e in entries:
        g=(e.get("group") or "").strip() or "Other"; group_counts[g]+=1
        m=e.get("meta") or {}
        for k,v in m.items():
            if v: qual[k]+=1
        if e.get("epg_id"): has_epg+=1
        if e.get("logo_present"): logos+=1
        text=(g+" "+(e.get("title") or ""))
        for c in set(country_rx.findall(text)): country_counts[c]+=1
    samples=[]
    if keep_names:
        from urllib.parse import urlparse
        for e in entries[:sample_name_max]:
            samples.append({"title": e.get("title"),
                            "group": e.get("group"),
                            "url_host_hash": urlparse(e.get("url","")).netloc})
    return {"total_channels": len(entries), "groups_top": group_counts.most_common(25),
            "countries_counts": country_counts.most_common(30), "quality_flags": dict(qual),
            "with_epg": has_epg, "with_logo": logos, "samples": samples}

def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--m3u", required=True)
    ap.add_argument("--xmltv")
    ap.add_argument("--out", default="summary.json")
    ap.add_argument("--keep-names", action="store_true")
    a=ap.parse_args()
    entries=parse_m3u(a.m3u)
    out={"m3u_summary": summarize(entries, keep_names=a.keep_names),
         "xmltv_summary": {}}
    Path(a.out).write_text(json.dumps(out, indent=2), encoding="utf-8")
    print(f"Wrote {a.out} — OK")
if __name__=="__main__": main()
