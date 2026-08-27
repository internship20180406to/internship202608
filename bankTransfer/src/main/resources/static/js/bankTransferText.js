// 「見た目が違うだけの同じ字」を1つの形へ寄せる道具。
// 読み込むと window.bankTransferText に入る。使う場所は3つ。
//   ・口座名義の欄   打った字をその場で半角カタカナに直す
//   ・数字の欄       全角で打たれた数字を半角として受け取る（口座番号・金額）
//   ・一覧の絞り込み ひらがなで打っても半角カタカナの名義に当てる
// どれも同じ仕事なので、寄せ方はここ1か所に置く。
(() => {
    // 全角と半角を同じ並び順で持つ。1字ずつ対応表を書くより短く、
    // 並びがずれたときにも気付きやすい（両方の長さが違えば必ず壊れる）
    const FULL = 'アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンァィゥェォッャュョー・゛゜';
    const HALF = 'ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜｦﾝｧｨｩｪｫｯｬｭｮｰ･ﾞﾟ';

    // 濁点・半濁点の付いた字は、半角では「清音＋点」の2字になる。
    // いったん清音に直してから点を足し直す
    const VOICED      = 'ガギグゲゴザジズゼゾダヂヅデドバビブベボヴ';
    const VOICED_BASE = 'カキクケコサシスセソタチツテトハヒフヘホウ';
    const SEMI        = 'パピプペポ';
    const SEMI_BASE   = 'ハヒフヘホ';

    // ひらがなはカタカナと同じ並びで、コードが 0x60 だけ小さい。
    // 対応するカタカナが無い「ゝ ゞ」は範囲の外なのでそのまま
    const toKatakana = (ch) => {
        const code = ch.charCodeAt(0);
        return code >= 0x3041 && code <= 0x3096
                ? String.fromCharCode(code + 0x60)
                : ch;
    };

    // 半角カタカナに直せるなら直した文字を、直せないなら null を返す
    const toHalfKana = (ch) => {
        const kana = toKatakana(ch);
        const voiced = VOICED.indexOf(kana);
        if (voiced >= 0) {
            return HALF[FULL.indexOf(VOICED_BASE[voiced])] + '\uFF9E';
        }
        const semi = SEMI.indexOf(kana);
        if (semi >= 0) {
            return HALF[FULL.indexOf(SEMI_BASE[semi])] + '\uFF9F';
        }
        const index = FULL.indexOf(kana);
        return index >= 0 ? HALF[index] : null;
    };

    // 口座名義に使える字かどうか。サーバ側の @Pattern と同じ範囲にしておく。
    // FF65-FF9F は 中黒・カタカナ・長音符・濁点・半濁点
    const isName = (ch) => /[\uFF65-\uFF9F ]/.test(ch);

    // 名義の欄用。半角カタカナに直せた分だけを返し、
    // 直せなかった字（漢字・英字など）は捨てて dropped に入れる。
    // 捨てたことを画面で知らせたいので、捨てた字も一緒に返す
    const toHalfWidthName = (text) => {
        let converted = '';
        let dropped = '';
        for (const ch of text) {
            if (ch === ' ' || ch === '\u3000') {
                converted += ' ';
            } else if (isName(ch)) {
                converted += ch;
            } else {
                const half = toHalfKana(ch);
                if (half === null) {
                    dropped += ch;
                } else {
                    converted += half;
                }
            }
        }
        return { text: converted, dropped: dropped };
    };

    // 絞り込み用。1行の中に半角カタカナの名義・漢字の支店名・数字の口座番号が
    // 混ざっているので、寄せられる字だけ寄せて、それ以外はそのまま残す。
    // 空白は入れ方が人によって違うため、探す側・探される側の両方から抜く
    const normalize = (text) => {
        let result = '';
        for (const ch of text) {
            const code = ch.codePointAt(0);
            if (ch === ' ' || ch === '\u3000') {
                continue;
            }
            // 全角の英数字と記号は「！」から「～」まで半角と同じ並びなので、まとめて寄せる
            if (code >= 0xFF01 && code <= 0xFF5E) {
                result += String.fromCharCode(code - 0xFEE0);
                continue;
            }
            const half = toHalfKana(ch);
            result += half === null ? ch : half;
        }
        return result.toLowerCase();
    };

    // ローマ字とカタカナの対応。IMEが使う綴りの揺れ（si/shi、ji/zi など）は
    // どちらで打っても同じ字になるよう、両方を表に入れている
    const ROMAJI = {
        a: 'ア', i: 'イ', u: 'ウ', e: 'エ', o: 'オ',
        ka: 'カ', ki: 'キ', ku: 'ク', ke: 'ケ', ko: 'コ',
        sa: 'サ', shi: 'シ', si: 'シ', su: 'ス', se: 'セ', so: 'ソ',
        ta: 'タ', chi: 'チ', ti: 'チ', tsu: 'ツ', tu: 'ツ', te: 'テ', to: 'ト',
        na: 'ナ', ni: 'ニ', nu: 'ヌ', ne: 'ネ', no: 'ノ',
        ha: 'ハ', hi: 'ヒ', fu: 'フ', hu: 'フ', he: 'ヘ', ho: 'ホ',
        ma: 'マ', mi: 'ミ', mu: 'ム', me: 'メ', mo: 'モ',
        ya: 'ヤ', yu: 'ユ', yo: 'ヨ',
        ra: 'ラ', ri: 'リ', ru: 'ル', re: 'レ', ro: 'ロ',
        wa: 'ワ', wo: 'ヲ', nn: 'ン',
        ga: 'ガ', gi: 'ギ', gu: 'グ', ge: 'ゲ', go: 'ゴ',
        za: 'ザ', ji: 'ジ', zi: 'ジ', zu: 'ズ', ze: 'ゼ', zo: 'ゾ',
        da: 'ダ', di: 'ヂ', du: 'ヅ', de: 'デ', do: 'ド',
        ba: 'バ', bi: 'ビ', bu: 'ブ', be: 'ベ', bo: 'ボ',
        pa: 'パ', pi: 'ピ', pu: 'プ', pe: 'ペ', po: 'ポ',
        kya: 'キャ', kyu: 'キュ', kyo: 'キョ',
        sha: 'シャ', shu: 'シュ', sho: 'ショ', sya: 'シャ', syu: 'シュ', syo: 'ショ',
        cha: 'チャ', chu: 'チュ', cho: 'チョ', tya: 'チャ', tyu: 'チュ', tyo: 'チョ',
        nya: 'ニャ', nyu: 'ニュ', nyo: 'ニョ',
        hya: 'ヒャ', hyu: 'ヒュ', hyo: 'ヒョ',
        mya: 'ミャ', myu: 'ミュ', myo: 'ミョ',
        rya: 'リャ', ryu: 'リュ', ryo: 'リョ',
        gya: 'ギャ', gyu: 'ギュ', gyo: 'ギョ',
        ja: 'ジャ', ju: 'ジュ', jo: 'ジョ', jya: 'ジャ', jyu: 'ジュ', jyo: 'ジョ',
        zya: 'ジャ', zyu: 'ジュ', zyo: 'ジョ',
        bya: 'ビャ', byu: 'ビュ', byo: 'ビョ',
        pya: 'ピャ', pyu: 'ピュ', pyo: 'ピョ',
        fa: 'ファ', fi: 'フィ', fe: 'フェ', fo: 'フォ',
        va: 'ヴァ', vi: 'ヴィ', vu: 'ヴ', ve: 'ヴェ', vo: 'ヴォ',
        she: 'シェ', che: 'チェ', je: 'ジェ',
        xa: 'ァ', xi: 'ィ', xu: 'ゥ', xe: 'ェ', xo: 'ォ', xtu: 'ッ',
        xya: 'ャ', xyu: 'ュ', xyo: 'ョ',
        la: 'ァ', li: 'ィ', lu: 'ゥ', le: 'ェ', lo: 'ォ', ltu: 'ッ'
    };

    // 表のどれかの先頭になっている並びだけ、続きを待つ価値がある。
    // 「ky」は待つが「q」は待っても何にもならない
    const PREFIXES = (() => {
        const set = new Set();
        for (const key of Object.keys(ROMAJI)) {
            for (let i = 1; i <= key.length; i++) {
                set.add(key.slice(0, i));
            }
        }
        return set;
    })();

    const VOWELS = 'aiueo';
    const isLetter = (ch) => (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');

    // ローマ字を打った端からカタカナにする。
    // IMEを開いている間はブラウザが欄の中身を書き換えさせてくれないので、
    // 「打った字が最初から半角」にするには、IMEを使わずに打ってもらうしかない。
    // 「k」「ky」のような打ちかけは、続きが来るまでそのまま残す。
    // resolve が真のとき（欄を離れる・送信する）は、残った打ちかけを片付ける
    const fromRomaji = (text, resolve) => {
        let out = '';
        let dropped = '';
        let buf = '';

        // buf の先頭から、当てはまる並びを取り出せるだけ取り出す
        const consume = () => {
            let moved = true;
            while (moved && buf !== '') {
                moved = false;
                const lower = buf.toLowerCase();
                // 促音。同じ子音が2つ続いたら「ッ」（kka → ｯｶ）
                if (lower.length >= 2 && lower[0] === lower[1]
                        && lower[0] !== 'n' && VOWELS.indexOf(lower[0]) < 0) {
                    out += 'ッ';
                    buf = buf.slice(1);
                    moved = true;
                    continue;
                }
                // 撥音。n の次が母音でも y でも n でもなければ「ン」（kanda → ｶﾝﾀﾞ）
                if (lower.length >= 2 && lower[0] === 'n' && lower[1] !== 'y' && lower[1] !== 'n'
                        && VOWELS.indexOf(lower[1]) < 0) {
                    out += 'ン';
                    buf = buf.slice(1);
                    moved = true;
                    continue;
                }
                // 長い綴りから先に当てる。先に「ki」を当ててしまうと「kya」に届かない
                for (let len = Math.min(3, buf.length); len >= 1; len--) {
                    const kana = ROMAJI[lower.slice(0, len)];
                    if (kana !== undefined) {
                        out += kana;
                        buf = buf.slice(len);
                        moved = true;
                        break;
                    }
                }
            }
        };

        // 続きが来ても何にもならない字は、待たずに落とす
        const settle = () => {
            while (buf !== '' && !PREFIXES.has(buf.toLowerCase())) {
                dropped += buf[0];
                buf = buf.slice(1);
                consume();
            }
        };

        // 打ちかけのローマ字の後始末。打っている間はそのまま残し、
        // 欄を離れるときだけ片付ける（最後の「n」は続けようがないので「ン」にする）
        const endRun = (finish) => {
            const rest = buf;
            buf = '';
            if (rest === '' || !finish) {
                return rest;
            }
            if (rest.toLowerCase() === 'n') {
                return 'ン';
            }
            dropped += rest;
            return '';
        };

        for (const ch of text) {
            if (isLetter(ch)) {
                buf += ch;
                consume();
                settle();
                continue;
            }
            out += endRun(resolve === true);
            // 「-」は長音のつもりで打たれる
            out += ch === '-' ? 'ー' : ch;
        }
        out += endRun(resolve === true);
        return { text: out, dropped: dropped };
    };

    // 全角で打たれた数字を半角に直す。打った本人には「１２３」も数字なので、
    // 数字として受け取る。数字以外はそのまま残し、捨てるかどうかは呼ぶ側が決める
    const toHalfDigits = (text) => text.replace(/[\uFF10-\uFF19]/g,
            (ch) => String.fromCharCode(ch.charCodeAt(0) - 0xFEE0));

    window.bankTransferText = {
        toHalfWidthName: toHalfWidthName,
        toHalfDigits: toHalfDigits,
        normalize: normalize,
        fromRomaji: fromRomaji
    };
})();
