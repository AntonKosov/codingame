const codinggame_1d_spreadsheet = require("./app");

const testRunner = (cells, expectedResult) => {
    const input = (() => {
        const inputLines = [cells.length, ...cells];

        let inputIndex = 0;

        return () => { return inputLines[inputIndex++]; };
    })();

    let expectedResultIndex = 0;
    const output = cellValue => {
        expect(parseInt(cellValue)).toBe(expectedResult[expectedResultIndex++]);
    };

    codinggame_1d_spreadsheet.run(input, output);

    expect(expectedResultIndex).toBe(expectedResult.length);
};

test('No cells', () => {
    testRunner([], []);
});

test('Values only', () => {
    testRunner(
        ["VALUE 3 _", "VALUE 5 _"],
        [3, 5]
    );
});

test('3 + 6 function', () => {
    testRunner(["ADD 3 6"], [9]);
});

test('4 - 7 function', () => {
    testRunner(["SUB 4 7"], [-3]);
});

test('4 * 5 function', () => {
    testRunner(["MULT 4 5"], [20]);
});

test('ref * 5 function', () => {
    testRunner(
        ["MULT $1 5", "VALUE 5 _"],
        [25, 5]);
});

test('5 + ref function', () => {
    testRunner(
        ["VALUE 4 _", "ADD 5 $0"],
        [4, 9]);
});

test('ref - ref function', () => {
    testRunner(
        ["VALUE 4 _", "VALUE 9 _", "SUB $1 $0"],
        [4, 9, 5]);
});

test('ref(ref) * ref(ref) function', () => {
    testRunner(
        [
            "MULT $1 $2", // 3 * 17 = 51
            "SUB $3 $4",  // 3
            "ADD $3 $4",  // 17
            "VALUE 10 _", // 10
            "VALUE 7 _"   // 7
        ],
        [51, 3, 17, 10, 7]);
});