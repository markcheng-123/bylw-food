#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path
from xml.sax.saxutils import escape


ROOT = Path(__file__).resolve().parents[1]
ASSET_DIR = ROOT / "uploads" / "seed-assets"


def _svg_canvas(
    title: str,
    subtitle: str,
    desc: str,
    bg1: str = "#f6e2c7",
    bg2: str = "#e9bd8f",
    badge: str | None = None,
) -> str:
    title = escape(title)
    subtitle = escape(subtitle)
    desc = escape(desc)
    badge_markup = ""
    if badge:
        badge = escape(badge)
        badge_markup = f"""
  <g transform='translate(980 120)'>
    <rect x='-130' y='-48' width='260' height='96' rx='48' fill='rgba(146,84,31,.16)'/>
    <text x='0' y='16' text-anchor='middle' font-size='36' font-family='Microsoft YaHei, SimHei, Arial' fill='#7a4c1d' font-weight='700'>{badge}</text>
  </g>"""

    return f"""<svg xmlns='http://www.w3.org/2000/svg' width='1200' height='800' viewBox='0 0 1200 800'>
  <defs>
    <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>
      <stop offset='0%' stop-color='{bg1}'/>
      <stop offset='100%' stop-color='{bg2}'/>
    </linearGradient>
  </defs>
  <rect width='1200' height='800' fill='url(#g)'/>
  <rect x='70' y='70' width='1060' height='660' rx='26' fill='rgba(255,255,255,0.93)'/>
  <text x='120' y='250' font-size='72' font-family='Microsoft YaHei, SimHei, Arial' fill='#2f2f2f' font-weight='700'>{title}</text>
  <text x='120' y='340' font-size='38' font-family='Microsoft YaHei, SimHei, Arial' fill='#4f4f4f'>{subtitle}</text>
  <text x='120' y='410' font-size='30' font-family='Microsoft YaHei, SimHei, Arial' fill='#666'>{desc}</text>{badge_markup}
</svg>
"""


def _svg_doc(
    doc_title: str,
    merchant: str,
    number: str,
    address: str,
    valid_from: str,
    valid_to: str,
    stamp: str,
    bg1: str,
    bg2: str,
) -> str:
    return f"""<svg xmlns='http://www.w3.org/2000/svg' width='1200' height='800' viewBox='0 0 1200 800'>
  <defs>
    <linearGradient id='bg' x1='0' y1='0' x2='1' y2='1'>
      <stop offset='0%' stop-color='{bg1}'/>
      <stop offset='100%' stop-color='{bg2}'/>
    </linearGradient>
  </defs>
  <rect width='1200' height='800' fill='url(#bg)'/>
  <rect x='52' y='52' width='1096' height='696' rx='16' fill='rgba(255,255,255,0.92)' stroke='#d7be8a' stroke-width='4'/>
  <text x='600' y='130' text-anchor='middle' font-size='56' font-family='Microsoft YaHei, SimHei, Arial' fill='#7a1f1f' font-weight='700'>{escape(doc_title)}</text>
  <text x='600' y='175' text-anchor='middle' font-size='24' font-family='Microsoft YaHei, SimHei, Arial' fill='#9a5d2e'>市场监督管理局电子存档副本</text>
  <g font-family='Microsoft YaHei, SimHei, Arial' fill='#2f2f2f' font-size='30'>
    <text x='110' y='260'>名    称：{escape(merchant)}</text>
    <text x='110' y='320'>统一社会信用代码：{escape(number)}</text>
    <text x='110' y='380'>法定代表人：王强</text>
    <text x='110' y='440'>住    所：{escape(address)}</text>
    <text x='110' y='500'>成立日期：{escape(valid_from)}</text>
    <text x='110' y='560'>有效期限：{escape(valid_to)}</text>
    <text x='110' y='620'>经营范围：餐饮服务、预包装食品销售</text>
  </g>
  <g>
    <circle cx='980' cy='590' r='102' fill='none' stroke='#c82828' stroke-width='8'/>
    <text x='980' y='585' text-anchor='middle' font-size='28' font-family='Microsoft YaHei, SimHei, Arial' fill='#c82828' font-weight='700'>{escape(stamp)}</text>
    <text x='980' y='622' text-anchor='middle' font-size='28' font-family='Microsoft YaHei, SimHei, Arial' fill='#c82828' font-weight='700'>专用章</text>
  </g>
  <text x='860' y='700' font-size='24' font-family='Microsoft YaHei, SimHei, Arial' fill='#5d5d5d'>发证日期：2025年03月18日</text>
</svg>
"""


def regenerate() -> None:
    ASSET_DIR.mkdir(parents=True, exist_ok=True)

    posts = [
        ("post_root3_hotdrynoodles.svg", "\u70ed\u5e72\u9762", "root3 \u7528\u6237\u53d1\u5e03", "\u829d\u9ebb\u9171\u9999\u6d53\u3001\u841d\u535c\u4e01\u8106\u723d", "\u6b66\u6c49\u65e9\u9910"),
        ("post_root4_hotpot.svg", "\u725b\u6cb9\u706b\u9505", "root4 \u7528\u6237\u53d1\u5e03", "\u9ebb\u8fa3\u9505\u5e95\u6d53\u90c1\u3001\u6bdb\u809a\u9c9c\u8106", "\u91cd\u5e86\u98ce\u5473"),
        ("post_root5_bbq.svg", "\u70ad\u70e4\u62fc\u76d8", "root5 \u7528\u6237\u53d1\u5e03", "\u9c7f\u9c7c\u4e0e\u8089\u4e32\u706b\u5019\u6070\u5230\u597d\u5904", "\u591c\u5bb5\u4eba\u6c14"),
        ("post_root6_shengjian.svg", "\u751f\u714e\u5305", "root6 \u7528\u6237\u53d1\u5e03", "\u5e95\u90e8\u7126\u9999\u9171\u6c41\u5145\u76c8", "\u5df7\u53e3\u65e9\u70b9"),
        ("post_root7_cake.svg", "\u5976\u6cb9\u86cb\u7cd5", "root7 \u7528\u6237\u53d1\u5e03", "\u7ec6\u817b\u86cb\u7cd5\u4f53\u914d\u8f7b\u76c8\u5976\u6cb9", "\u4e0b\u5348\u8336"),
        ("post_root8_bbq.svg", "\u6865\u5934\u70e7\u70e4", "root8 \u7528\u6237\u53d1\u5e03", "\u70ad\u706b\u9999\u6c14\u8db3\u3001\u6492\u6599\u5c42\u6b21\u5206\u660e", "\u8def\u8fb9\u70df\u706b\u6c14"),
        ("post_root9_beefhotpot.svg", "\u725b\u8089\u706b\u9505", "root9 \u7528\u6237\u53d1\u5e03", "\u732a\u9aa8\u9ad8\u6c64\u642d\u914d\u9c9c\u5207\u725b\u8089", "\u805a\u9910\u5fc5\u70b9"),
        ("post_root10_nuomiji.svg", "\u7cef\u7c73\u9e21", "root10 \u7528\u6237\u53d1\u5e03", "\u8377\u53f6\u9999\u6e17\u5165\u7cef\u7c73\u4e0e\u9e21\u8089", "\u5e7f\u5f0f\u70b9\u5fc3"),
        ("post_root11_mian.svg", "\u62cc\u9762", "root11 \u7528\u6237\u53d1\u5e03", "\u9171\u6c41\u5305\u88f9\u9762\u6761\u53e3\u611f\u5f39\u7259", "\u5c0f\u9986\u62db\u724c"),
        ("post_root12_tart.svg", "\u829d\u58eb\u631e", "root12 \u7528\u6237\u53d1\u5e03", "\u633b\u76ae\u9999\u9165\u3001\u5185\u9985\u7ec6\u6ed1\u6d53\u90c1", "\u751c\u54c1\u63a8\u8350"),
        ("post_root13_skewer.svg", "\u725b\u677f\u7b4b", "root13 \u7528\u6237\u53d1\u5e03", "\u9999\u8fa3\u5165\u5473\u3001\u8033\u6735\u8089\u611f\u5341\u8db3", "\u4e0b\u9152\u5c0f\u4e32"),
        ("post_root14_tomatohotpot.svg", "\u756a\u8304\u9505\u5e95", "root14 \u7528\u6237\u53d1\u5e03", "\u9178\u751c\u6e05\u723d\u3001\u6d82\u83dc\u8001\u5c11\u7686\u5b9c", "\u6e29\u548c\u9505\u5e95"),
        ("post_root15_jiangxiangbing.svg", "\u9171\u9999\u997c", "root15 \u7528\u6237\u53d1\u5e03", "\u5916\u8106\u91cc\u8f6f\u3001\u9171\u9999\u56de\u7518", "\u8857\u5934\u5c0f\u5403"),
        ("post_root16_yangzhiganlu.svg", "\u6768\u679d\u7518\u9732", "root16 \u7528\u6237\u53d1\u5e03", "\u8292\u679c\u4e0e\u897f\u67da\u7ec6\u817b\u6e05\u723d", "\u996e\u54c1\u70ed\u5356"),
        ("post_root17_beefnoodle.svg", "\u725b\u8089\u9762", "root17 \u7528\u6237\u53d1\u5e03", "\u725b\u8089\u9171\u9999\u6d53\u90c1\u3001\u9762\u6761\u52b2\u9053", "\u9762\u9986\u62db\u724c"),
    ]

    for name, title, subtitle, desc, badge in posts:
        svg = _svg_canvas(title, subtitle, desc, badge=badge)
        (ASSET_DIR / name).write_text(svg, encoding="utf-8")

    dish_cards = [
        ("dish_root3_hotdrynoodles.svg", "\u62db\u724c\u70ed\u5e72\u9762", "root3 \u83dc\u54c1\u56fe", "\u829d\u9ebb\u9171+\u841d\u535c\u4e01+\u8471\u82b1", "\u9762\u98df"),
        ("dish_root4_hotpot.svg", "\u725b\u6cb9\u7ea2\u6c64\u9505", "root4 \u83dc\u54c1\u56fe", "\u725b\u6cb9+\u82b1\u6912+\u8fa3\u6912+\u725b\u9aa8\u6c64", "\u706b\u9505"),
        ("dish_root5_bbq.svg", "\u70ad\u70e4\u62fc\u76d8", "root5 \u83dc\u54c1\u56fe", "\u9e21\u7fc5+\u725b\u8089\u4e32+\u9c7f\u9c7c", "\u70e7\u70e4"),
    ]
    for name, title, subtitle, desc, badge in dish_cards:
        svg = _svg_canvas(title, subtitle, desc, bg1="#fce8cf", bg2="#f3c799", badge=badge)
        (ASSET_DIR / name).write_text(svg, encoding="utf-8")

    docs = [
        (
            "license_root3.svg",
            "\u8425\u4e1a\u6267\u7167",
            "\u6c5f\u57ce\u98ce\u5473\u9986",
            "91420106MA4K5R3X8A",
            "\u6e56\u5317\u7701 \u6b66\u6c49\u5e02 \u6b66\u660c\u533a\u4e2d\u5317\u8def88\u53f7",
            "2024\u5e7403\u670818\u65e5",
            "2024\u5e7403\u670818\u65e5\u81f32034\u5e7403\u670817\u65e5",
            "\u6b66\u6c49\u5e02\u573a\u76d1\u7ba1\u5c40",
            "#f9efd7",
            "#f2dfb7",
        ),
        (
            "license_root4.svg",
            "\u8425\u4e1a\u6267\u7167",
            "\u6e1d\u5473\u706b\u9505\u5c40",
            "91500103MA5U8P1N2B",
            "\u91cd\u5e86\u5e02 \u6e1d\u4e2d\u533a \u89e3\u653e\u7891\u6b65\u884c\u88579\u53f7",
            "2024\u5e7404\u670802\u65e5",
            "2024\u5e7404\u670802\u65e5\u81f32034\u5e7404\u670801\u65e5",
            "\u91cd\u5e86\u5e02\u573a\u76d1\u7ba1\u5c40",
            "#f9efd7",
            "#f2dfb7",
        ),
        (
            "license_root5.svg",
            "\u8425\u4e1a\u6267\u7167",
            "\u5cad\u5357\u591c\u5bb5\u6863",
            "91440106MA59T7H6XQ",
            "\u5e7f\u4e1c\u7701 \u5e7f\u5dde\u5e02 \u5929\u6cb3\u533a\u4f53\u80b2\u897f\u8def66\u53f7",
            "2024\u5e7405\u670811\u65e5",
            "2024\u5e7405\u670811\u65e5\u81f32034\u5e7405\u670810\u65e5",
            "\u5e7f\u5dde\u5e02\u573a\u76d1\u7ba1\u5c40",
            "#f9efd7",
            "#f2dfb7",
        ),
        (
            "health_root3.svg",
            "\u98df\u54c1\u7ecf\u8425\u8bb8\u53ef\u8bc1",
            "\u6c5f\u57ce\u98ce\u5473\u9986",
            "JY24201060000031",
            "\u6e56\u5317\u7701 \u6b66\u6c49\u5e02 \u6b66\u660c\u533a\u4e2d\u5317\u8def88\u53f7",
            "2025\u5e7401\u670816\u65e5",
            "2025\u5e7401\u670816\u65e5\u81f32030\u5e7401\u670815\u65e5",
            "\u98df\u5b89\u8bb8\u53ef",
            "#e7f4ea",
            "#cfe9d6",
        ),
        (
            "health_root4.svg",
            "\u98df\u54c1\u7ecf\u8425\u8bb8\u53ef\u8bc1",
            "\u6e1d\u5473\u706b\u9505\u5c40",
            "JY25001030000028",
            "\u91cd\u5e86\u5e02 \u6e1d\u4e2d\u533a \u89e3\u653e\u7891\u6b65\u884c\u88579\u53f7",
            "2025\u5e7402\u670803\u65e5",
            "2025\u5e7402\u670803\u65e5\u81f32030\u5e7402\u670802\u65e5",
            "\u98df\u5b89\u8bb8\u53ef",
            "#e7f4ea",
            "#cfe9d6",
        ),
        (
            "health_root5.svg",
            "\u98df\u54c1\u7ecf\u8425\u8bb8\u53ef\u8bc1",
            "\u5cad\u5357\u591c\u5bb5\u6863",
            "JY24401060000042",
            "\u5e7f\u4e1c\u7701 \u5e7f\u5dde\u5e02 \u5929\u6cb3\u533a\u4f53\u80b2\u897f\u8def66\u53f7",
            "2025\u5e7402\u670828\u65e5",
            "2025\u5e7402\u670828\u65e5\u81f32030\u5e7402\u670827\u65e5",
            "\u98df\u5b89\u8bb8\u53ef",
            "#e7f4ea",
            "#cfe9d6",
        ),
    ]

    for name, doc_title, merchant, number, address, valid_from, valid_to, stamp, bg1, bg2 in docs:
        svg = _svg_doc(doc_title, merchant, number, address, valid_from, valid_to, stamp, bg1, bg2)
        (ASSET_DIR / name).write_text(svg, encoding="utf-8")

    chefs = [
        ("chef_root3_zhou.svg", "\u5468\u5e08\u5085", "\u4e3b\u7406\u53a8\u5e08"),
        ("chef_root3_liu.svg", "\u5218\u5e08\u5085", "\u9762\u70b9\u4e3b\u7ba1"),
        ("chef_root4_chen.svg", "\u9648\u5e08\u5085", "\u9505\u5e95\u7814\u53d1"),
        ("chef_root4_he.svg", "\u4f55\u5e08\u5085", "\u540e\u53a8\u603b\u7ba1"),
        ("chef_root5_huang.svg", "\u9ec4\u5e08\u5085", "\u7ca4\u83dc\u4e3b\u53a8"),
        ("chef_root5_yang.svg", "\u6768\u5e08\u5085", "\u591c\u5bb5\u6863\u4e3b\u7406"),
    ]
    for name, chef_name, chef_title in chefs:
        svg = _svg_canvas(
            chef_name,
            chef_title,
            "\u4ece\u4e1a12\u5e74\uff0c\u64c5\u957f\u5730\u65b9\u83dc\u4e0e\u51fa\u54c1\u628a\u63a7",
            bg1="#f0efe9",
            bg2="#ddd6c8",
            badge="\u53a8\u5e08",
        )
        (ASSET_DIR / name).write_text(svg, encoding="utf-8")


def verify() -> None:
    probes = {
        "post_root3_hotdrynoodles.svg": "\u70ed\u5e72\u9762",
        "post_root16_yangzhiganlu.svg": "\u6768\u679d\u7518\u9732",
        "post_root17_beefnoodle.svg": "\u725b\u8089\u9762",
        "license_root3.svg": "\u8425\u4e1a\u6267\u7167",
        "health_root3.svg": "\u98df\u54c1\u7ecf\u8425\u8bb8\u53ef\u8bc1",
        "chef_root3_liu.svg": "\u5218\u5e08\u5085",
    }
    for name, expected in probes.items():
        text = (ASSET_DIR / name).read_text(encoding="utf-8")
        if "???" in text or expected not in text:
            raise RuntimeError(f"verify failed: {name}")


if __name__ == "__main__":
    regenerate()
    verify()
    print(f"done: regenerated assets in {ASSET_DIR}")
