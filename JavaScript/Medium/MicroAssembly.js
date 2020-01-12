// https://www.codingame.com/training/medium/micro-assembly
(() => {
	let registers = {
		a: 0,
		b: 0,
		c: 0,
		d: 0
	};

	class Instruction {
		constructor(command, arg1, arg2, arg3) {
			let parseArg = arg => isNaN(arg) ? arg : parseInt(arg);

			this.command = command;
			this.arg1 = parseArg(arg1);
			this.arg2 = parseArg(arg2);
			this.arg3 = parseArg(arg3);
		}
	}

	let argValue = arg => isNaN(arg) ? registers[arg] : arg;

	let currentLine = 0;

	let commands = {
		"MOV": instruction => {
			registers[instruction.arg1] = argValue(instruction.arg2);
			currentLine++;
		},
		"ADD": instruction => {
			registers[instruction.arg1] = argValue(instruction.arg2) + argValue(instruction.arg3);
			currentLine++;
		},
		"SUB": instruction => {
			registers[instruction.arg1] = argValue(instruction.arg2) - argValue(instruction.arg3);
			currentLine++;
		},
		"JNE": instruction => {
			let arg2Value = argValue(instruction.arg2);
			let arg3Value = argValue(instruction.arg3);
			if (arg2Value === arg3Value) {
				currentLine++;
			} else {
				currentLine = instruction.arg1;
			}
		}
	};

	[registers.a, registers.b, registers.c, registers.d] = readline().split(' ').map(v => parseInt(v));

	let instructions = [];
	const lines = parseInt(readline());
	for (let i = 0; i < lines; i++) {
		const [command, arg1, arg2, arg3] = readline().split(' ');
		instructions.push(new Instruction(command, arg1, arg2, arg3));
	}

	while (currentLine < lines) {
		let instruction = instructions[currentLine];
		commands[instruction.command](instruction);
	}

	// Write an action using console.log()
	// To debug: console.error('Debug messages...');
	console.log(`${registers.a} ${registers.b} ${registers.c} ${registers.d}`);
})();