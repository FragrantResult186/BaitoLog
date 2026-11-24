/*
 * BaitoLog カレンダーの色分け（土・日・祝日）
 * シフト登録（ShiftEdit）とダッシュボード（StudentTop）で同じ判定を使う
 *
 * BaitoCalendar.holidayName("2026-11-23") → "勤労感謝の日"（祝日でなければ ""）
 * BaitoCalendar.dayClass("2026-11-23")    → "day-holiday" / "day-sun" / "day-sat" / ""
 *   （祝日は、土日と重なっても「祝日」の色を優先する）
 *
 * .calendar-day[data-date] の付いたマスには、読み込み後に自動で色の class と祝日名を付ける
 * 対応：2007年〜（祝日は法律の決まりどおりに計算。振替休日・国民の休日を含む）
 */
(function () {
  var cache = {};

  function pad(n) { return (n < 10 ? "0" : "") + n; }
  function key(y, m, d) { return y + "-" + pad(m) + "-" + pad(d); }

  // 第n月曜日の日付
  function nthMonday(y, m, n) {
    var first = new Date(y, m - 1, 1).getDay();            // 1日の曜日（0=日）
    var firstMonday = 1 + ((8 - first) % 7);
    return firstMonday + (n - 1) * 7;
  }
  // 春分・秋分の日（1980〜2099年）
  function equinox(y, base) {
    return Math.floor(base + 0.242194 * (y - 1980) - Math.floor((y - 1980) / 4));
  }

  function build(y) {
    var h = {};
    function add(m, d, name) { h[key(y, m, d)] = name; }

    add(1, 1, "元日");
    add(1, nthMonday(y, 1, 2), "成人の日");
    add(2, 11, "建国記念の日");
    if (y >= 2020) { add(2, 23, "天皇誕生日"); }
    add(3, equinox(y, 20.8431), "春分の日");
    add(4, 29, "昭和の日");
    add(5, 3, "憲法記念日");
    add(5, 4, "みどりの日");
    add(5, 5, "こどもの日");
    if (y === 2019) { add(5, 1, "休日（天皇の即位）"); }

    // 海の日（2020年・2021年は東京オリンピックのため移動）
    if (y === 2020) { add(7, 23, "海の日"); }
    else if (y === 2021) { add(7, 22, "海の日"); }
    else { add(7, nthMonday(y, 7, 3), "海の日"); }

    // 山の日
    if (y === 2020) { add(8, 10, "山の日"); }
    else if (y === 2021) { add(8, 8, "山の日"); }
    else { add(8, 11, "山の日"); }

    add(9, nthMonday(y, 9, 3), "敬老の日");
    add(9, equinox(y, 23.2488), "秋分の日");

    // スポーツの日（2020年・2021年は移動）
    if (y === 2020) { add(7, 24, "スポーツの日"); }
    else if (y === 2021) { add(7, 23, "スポーツの日"); }
    else { add(10, nthMonday(y, 10, 2), "スポーツの日"); }

    if (y === 2019) { add(10, 22, "即位礼正殿の儀"); }
    add(11, 3, "文化の日");
    add(11, 23, "勤労感謝の日");

    // 国民の休日（前の日も次の日も祝日の平日）
    for (var m = 1; m <= 12; m++) {
      var last = new Date(y, m, 0).getDate();
      for (var d = 1; d <= last; d++) {
        var k = key(y, m, d);
        if (h[k]) { continue; }
        var dow = new Date(y, m - 1, d).getDay();
        if (dow === 0) { continue; }
        var prev = new Date(y, m - 1, d - 1);
        var next = new Date(y, m - 1, d + 1);
        if (h[key(prev.getFullYear(), prev.getMonth() + 1, prev.getDate())] &&
            h[key(next.getFullYear(), next.getMonth() + 1, next.getDate())]) {
          h[k] = "国民の休日";
        }
      }
    }

    // 振替休日（日曜日の祝日は、次の祝日でない日が休みになる）
    var base = Object.keys(h);
    for (var i = 0; i < base.length; i++) {
      var p = base[i].split("-");
      var dt = new Date(+p[0], +p[1] - 1, +p[2]);
      if (dt.getDay() !== 0) { continue; }
      do {
        dt = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate() + 1);
      } while (h[key(dt.getFullYear(), dt.getMonth() + 1, dt.getDate())]);
      h[key(dt.getFullYear(), dt.getMonth() + 1, dt.getDate())] = "振替休日";
    }
    return h;
  }

  function holidayName(dateStr) {
    var y = parseInt(dateStr.substring(0, 4), 10);
    if (!cache[y]) { cache[y] = build(y); }
    return cache[y][dateStr] || "";
  }

  function dayClass(dateStr) {
    if (holidayName(dateStr)) { return "day-holiday"; }
    var p = dateStr.split("-");
    var dow = new Date(+p[0], +p[1] - 1, +p[2]).getDay();
    if (dow === 0) { return "day-sun"; }
    if (dow === 6) { return "day-sat"; }
    return "";
  }

  window.BaitoCalendar = { holidayName: holidayName, dayClass: dayClass };

  // 表（ShiftEdit）のマスに、色の class と祝日名を付ける
  document.addEventListener("DOMContentLoaded", function () {
    var cells = document.querySelectorAll(".calendar-day[data-date]");
    for (var i = 0; i < cells.length; i++) {
      var date = cells[i].getAttribute("data-date");
      var cls = dayClass(date);
      if (cls) { cells[i].classList.add(cls); }
      var name = holidayName(date);
      if (name) { cells[i].title = name; }
    }
  });
})();