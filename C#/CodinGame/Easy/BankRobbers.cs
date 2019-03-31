using System;
using System.Collections.Generic;

namespace CodinGame.Easy
{
    public class BankRobbers
    {
        // ReSharper disable once UnusedMember.Local
        private static void Main()
        {
            List<Vault> vaults = new List<Vault>();
            // ReSharper disable once AssignNullToNotNullAttribute
            int numberOfRobbers = int.Parse(Console.ReadLine());
            // ReSharper disable once AssignNullToNotNullAttribute
            int numberOfVaults = int.Parse(Console.ReadLine());
            for (int i = 0; i < numberOfVaults; i++)
            {
                // ReSharper disable once PossibleNullReferenceException
                string[] inputs = Console.ReadLine().Split(' ');
                int length = int.Parse(inputs[0]);
                int digitsAhead = int.Parse(inputs[1]);
                vaults.Add(new Vault(length, digitsAhead));
            }

            BankRobbers bankRobbers = new BankRobbers(numberOfRobbers, vaults);

            // Write an action using Console.WriteLine()
            // To debug: Console.Error.WriteLine("Debug messages...");

            Console.WriteLine(bankRobbers.CalculateTime());
        }

        private const int NumberOfDigits = 10;
        private const int NumberOfCharacters = 5;
        
        private readonly int _numberOfRobbers;
        private readonly List<Vault> _vaults;

        public BankRobbers(int numberOfRobbers, List<Vault> vaults)
        {
            _numberOfRobbers = numberOfRobbers;
            _vaults = vaults;
        }

        // ReSharper disable once MemberCanBePrivate.Global
        public int CalculateTime()
        {
            List<int> robbers = new List<int>();
            for (int i = 0; i < _numberOfRobbers; i++)
                robbers.Add(0);

            int vaultIndex = 0;
            while (vaultIndex < _vaults.Count)
            {
                int cumulativeTime = robbers[0];
                robbers.RemoveAt(0);
                Vault vault = _vaults[vaultIndex];
                int digitsPermutations =
                    Pow(NumberOfDigits, vault.NumberOfDigitsAhead);
                int charactersPermutations =
                    Pow(NumberOfCharacters, vault.Length - vault.NumberOfDigitsAhead);
                int seconds = digitsPermutations * charactersPermutations;
                cumulativeTime += seconds;
                robbers.Add(cumulativeTime);
                robbers.Sort();
                vaultIndex++;
            }

            return robbers[robbers.Count - 1];
        }

        private static int Pow(int number, int power)
        {
            if (power < 0)
                throw new NotSupportedException("Power must be more or equal 0.");
            if (power == 0)
                return 1;

            int result = 1;
            for (int i = 0; i < power; i++)
                result *= number;

            return result;
        }

        public class Vault
        {
            public int Length { get; private set; }
            
            public int NumberOfDigitsAhead { get; private set; }

            public Vault(int length, int numberOfDigitsAhead)
            {
                Length = length;
                NumberOfDigitsAhead = numberOfDigitsAhead;
            }
        }
    }
}