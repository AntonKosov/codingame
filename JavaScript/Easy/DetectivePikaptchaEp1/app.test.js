const codingame_detective_pikaptcha_ep1 = require("./app");

const testRunner = (map, expectedMap) => {
    const input = (() => {
        const height = map.length;
        const width = typeof map[0] === 'undefined' ? 0 : map[0].length;
        const inputLines = [width + " " + height, ...map];

        let inputIndex = 0;

        return () => { return inputLines[inputIndex++]; };
    })();

    let expectedMapLineIndex = 0;
    const output = cellValue => {
        expect(cellValue).toBe(expectedMap[expectedMapLineIndex++]);
    };

    codingame_detective_pikaptcha_ep1.run(input, output);

    expect(expectedMapLineIndex).toBe(expectedMap.length);
};

test('One cell', () => {
    testRunner(["0"], ["0"]);
});

test('Two adjacent cells', () => {
    testRunner(["00"], ["11"]);
});

test('Three horizontal cells', () => {
    testRunner(["000"], ["121"]);
});

test('Three vertical cells', () => {
    testRunner(["0", "0", "0"], ["1", "2", "1"]);
});

test('Square with one wall', () => {
    testRunner(["00", "0#"], ["21", "1#"]);
});

test('Wall only', () => {
    testRunner(["##", "##"], ["##", "##"]);
});

test('Two chambers', () => {
    testRunner(
        [
            "#00###000",
            "000000000",
            "000##0000"
        ],
        [
            "#22###232",
            "244223443",
            "232##2332"
        ]);
});
