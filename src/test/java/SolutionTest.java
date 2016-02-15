import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Scanner.class, Solution.class})
public class SolutionTest {
    private PrintStream mOriginalOutPrintStream;

    private final LinkedList<String> mOutQueue = new LinkedList<>();

    private int mAnswer;

    @Before
    public void before() {
        mOriginalOutPrintStream = System.out;
    }

    @After
    public void after() {
        System.setOut(mOriginalOutPrintStream);
    }

    @Test(expected = SuccessException.class)
    public void sample() throws Exception {
        startTest(
                "......-...-..---.-----.-..-..-..",
                new String[] {
                        "HELL",
                        "HELLO",
                        "OWORLD",
                        "WORLD",
                        "TEST"
                },
                2
        );
    }

    @Test(expected = SuccessException.class)
    public void LongSequenceLargeDictionary() throws Exception {
        startTest(
                ".-...-.-...--......--....------.-.....---..--.-.----...-----.-.........-..--.-.-.----....--...-.--.--.--.--...-.-..--....--.--.-....--.-.-..--..-...-..-...-.......-.......-....--.--...-.-.......----....-.-.---..-.-.-.....-.-..----.--...-....--.-..-..-...-.........---...-.-.--..-.----....-..-.....--..-.-.-...........--.-.......--...-....---...-..---..-..--.--.....-.-.-..-.-.----.......-..-..-.----.--.-.-.---.-...-.....-....---..-..-..-----.-......-.-..---.-....--.-..-..--..-....-........-.-.....--...-....----.-......-...---.-...-.....-.-.-...----..-...........-.--.-.-.-....-..-..-.....-.-...-....-....-.-.-.-..--.---..-.-..-...--.-..--..-..-.--........-.-..-.-.----...---..-...-.-..-.-..--.-.-.-......-........--.-..-.-......---.-..-..-...--.-..-.-.........-...-..-...-.....--.-..-....--.----.-.-.-..-.--..----...--..-.-.------.....-..-.....----...--...--.-..-.-........--.-..-..--..-..-.-..-.--..--.-.-....-...-.-.----...-----.-.........--.-.----......-...-.-.-.-------..-....--..-.-.-...-.-.......-.-.-......-.-.----...",
                new String[]{
                        "SANTLE",
                        "ZUVFD",
                        "BXXFU",
                        "ESATOMI",
                        "HJXMQ",
                        "RJOEB",
                        "CQWVS",
                        "ROZNN",
                        "ITRNT",
                        "VGCVWU",
                        "WRQLY",
                        "UQNTV",
                        "TYDVR",
                        "NQKPBI",
                        "BMVZE",
                        "NYUVQX",
                        "RCODM",
                        "STSUD",
                        "ZGXPU",
                        "FCQCTZ",
                        "DXEXC",
                        "AXZBH",
                        "UHDWB",
                        "QTLHV",
                        "YJDGX",
                        "UOFBCG",
                        "BMIUH",
                        "BANNAK",
                        "MPLELALE",
                        "LRGKJU",
                        "DCQOQQ",
                        "WQCWA",
                        "TSJTB",
                        "BTMKQ",
                        "OUJOURSCE",
                        "ILISSPARL",
                        "HGQFY",
                        "KNXRZ",
                        "DEPERMETTANTDETRAN",
                        "JQXXBP",
                        "IVJUY",
                        "DGZRXJ",
                        "CEWBG",
                        "LNOQF",
                        "EIOHE",
                        "YNUKH",
                        "OHJXY",
                        "IXQNP",
                        "BTMMQ",
                        "UIHEXK",
                        "FALUN",
                        "SCYOJ",
                        "LDVGCI",
                        "HSTBA",
                        "XDMXL",
                        "LETDU",
                        "HQIJK",
                        "CASWT",
                        "ZKGUN",
                        "TYTMS",
                        "CNNZR",
                        "KGSWYW",
                        "KPVNGV",
                        "XYVHL",
                        "YPAOW",
                        "GOVFN",
                        "ROHQI",
                        "ZYGYV",
                        "LRVWBT",
                        "PSPTO",
                        "TCAVM",
                        "NSDVC",
                        "JIDAW",
                        "UBYGB",
                        "SCYNO",
                        "LBCWS",
                        "NRQXR",
                        "NOUQP",
                        "YFSPV",
                        "JZIDJ",
                        "BVASMD",
                        "BUICK",
                        "WWXAN",
                        "HMDYF",
                        "NZZUP",
                        "QKKHY",
                        "QSTOU",
                        "WBKDM",
                        "YYODA",
                        "YQPGL",
                        "DHDED",
                        "YFXZS",
                        "HEPCX",
                        "SGSJK",
                        "PLEKO",
                        "TALGU",
                        "DBWAA",
                        "DAUTR",
                        "DGIWG",
                        "CRHUR",
                        "SYKOU",
                        "VQIPZ",
                        "JKFAG",
                        "VGNWT",
                        "WLWZO",
                        "QSBKG",
                        "GGSTO",
                        "CFKYX",
                        "RXDOH",
                        "CASZX",
                        "GNPWT",
                        "YOOOE",
                        "OJFYK",
                        "WNFOJ",
                        "ZFIPK",
                        "RMQAH",
                        "WUYBI",
                        "TEENCORECEJO",
                        "CABRS",
                        "PUYJO",
                        "YHKQC",
                        "VPXPO",
                        "FERIR",
                        "MYGUH",
                        "SAFVR",
                        "JLBDE",
                        "VMFGR",
                        "RXHQF",
                        "LAPTZ",
                        "RFCHR",
                        "KYYXO",
                        "SFVEQ",
                        "MLHGF",
                        "UTQAC",
                        "AJMEF",
                        "EWYDF",
                        "BCSLM",
                        "UGQXC",
                        "DLGUXV",
                        "SGMWI",
                        "KVSQO",
                        "MBYWD",
                        "VCILU",
                        "NLYPJW",
                        "QZZJS",
                        "USUCR",
                        "NQVRO",
                        "AUTRAFI",
                        "TRESIGNAU",
                        "WFSQF",
                        "JPUKY",
                        "KUJWL",
                        "NCGUK",
                        "TFPVG",
                        "WQUNA",
                        "ZPJXO",
                        "UWYUB",
                        "HGQDL",
                        "REMORSEP",
                        "BTVAQ",
                        "TGFIQ",
                        "UENC",
                        "QNHPT",
                        "GZBWS",
                        "NXXCR",
                        "EJTJN",
                        "ATVTP",
                        "OISZU",
                        "STGMKK",
                        "FSBQC",
                        "NEEAK",
                        "JFSZL",
                        "ARZVZ",
                        "KPPRE",
                        "FEFCG",
                        "BUSIT",
                        "IVOQA",
                        "NWNNA",
                        "GIFIO",
                        "VDZNEP",
                        "AJLCY",
                        "HJFEU",
                        "UNOJN",
                        "BAXZM",
                        "AMRIY",
                        "MKAEX",
                        "JSVVWR",
                        "VFEAJ",
                        "QFVSE",
                        "UEKNB",
                        "SFBQB",
                        "PNEET",
                        "ODNZR",
                        "BCCOKO",
                        "OXAHB",
                        "JMYLN",
                        "MJUXJ",
                        "EOUDAPPELEN",
                        "CDAHG",
                        "IXWKO",
                        "VQOKR",
                        "VFOIF",
                        "RJTXC",
                        "WAUVAH",
                        "LEOIK",
                        "AAKWH",
                        "LXGSB",
                        "WRMITJ",
                        "CZPOAV",
                        "NEWKP",
                        "ISLTJ",
                        "NJGME",
                        "SVYZD",
                        "THLAP",
                        "DSLYRT",
                        "AAKUR",
                        "SGSBK",
                        "VRMGA",
                        "SJTBKA",
                        "NJOVT",
                        "TJMWXD",
                        "VENIE",
                        "RSFR",
                        "LLMQC",
                        "VGNBA",
                        "TWWWS",
                        "WPQABJ",
                        "JXPHS",
                        "YFRXR",
                        "BFSHH",
                        "ERNWU",
                        "LKVSO",
                        "JSEPP",
                        "WLWKM",
                        "XSCBBV",
                        "RJNNA",
                        "ROHYZ",
                        "JCAQA",
                        "ALORSENUT",
                        "QQHGD",
                        "DNPFY",
                        "TQCVA",
                        "SLTQH",
                        "INTDB",
                        "LSKHJN",
                        "ZUOJKQ",
                        "KQDOLF",
                        "GPIQB",
                        "JROVT",
                        "MQNJML",
                        "UTFJK",
                        "VUBUQ",
                        "CQSLZ",
                        "UVISUELFL",
                        "OXQJO",
                        "JCIZS",
                        "KUSPV",
                        "BDDUT",
                        "VDSKU",
                        "POMLH",
                        "MVPUK",
                        "CKGTG",
                        "FMMCY",
                        "ZVRXZ",
                        "JOSFTZ",
                        "GYKCKU",
                        "YXINT",
                        "CXRHC",
                        "CPMUE",
                        "CHIFFRES",
                        "JVQBC",
                        "NRCEL",
                        "MKQDX",
                        "ZZVZIS",
                        "LBLRTQ",
                        "UJDBZ",
                        "BXTFY",
                        "NLHPM",
                        "VWTNM",
                        "EFYWP",
                        "DIUTU",
                        "PDPDOR",
                        "TVIAUNSIGNALR",
                        "XOTDP",
                        "TYNTT",
                        "OPTIMISERLEC",
                        "GETNBI",
                        "AXPMC",
                        "WGYLG",
                        "GKSVGL",
                        "MYFYB",
                        "CSRUK",
                        "GNQHM",
                        "CNVIO",
                        "BHPRTP",
                        "VBCYG",
                        "FDFEFJ",
                        "PDTEV",
                        "CIXRH",
                        "URDESFRQUENC",
                        "BYUQP",
                        "GPEPK",
                        "TKOQC",
                        "AEGAI",
                        "GFDVTW",
                        "TOAQF",
                        "CQWEQ",
                        "MENCEDCLINERLENT",
                        "ADVHT",
                        "IAKQZ",
                        "WPRHJ",
                        "VODLOF",
                        "UURSL",
                        "HSAHM",
                        "SOVPD",
                        "IDEPC",
                        "TAXZTB",
                        "KXFLY",
                        "HPVRB",
                        "TRJCE",
                        "ADVKM",
                        "JPYSC",
                        "JMBIW",
                        "EZPWVF",
                        "RPRFR",
                        "WSWHG",
                        "EZARN",
                        "IOYJY",
                        "AZBOX",
                        "HNKKX",
                        "KIRAY",
                        "DSVDG",
                        "CDYRCM",
                        "ELSCG",
                        "MUYIP",
                        "INTHH",
                        "ZIQLW",
                        "BIOTG",
                        "VSHLH",
                        "UWMUPM",
                        "SMYXCQ",
                        "LNPFAA",
                        "ZGJWA",
                        "UZNMH",
                        "CWZMM",
                        "HEOQK",
                        "YIXWR",
                        "OTCJR",
                        "RHPQIJ",
                        "ZAEIC",
                        "SOVNI",
                        "YWZNRG",
                        "MENBP",
                        "VXNMR",
                        "YYNUI",
                        "XBYLR",
                        "UKIJN",
                        "KXFJT",
                        "LFXCV",
                        "HRASESC",
                        "OXGFXW",
                        "VZYIYL",
                        "GEBSY",
                        "EMORSEESTCO",
                        "UTFHO",
                        "VLSOC",
                        "UEKWS",
                        "YWVIC",
                        "MAYXO",
                        "JLAMA",
                        "KQVPV",
                        "CWNDQ",
                        "VERSUNCB",
                        "EXTELAIDE",
                        "HXKCL",
                        "MSMPC",
                        "YRZFG",
                        "KMAJF",
                        "JYMSI",
                        "UAFJD",
                        "OOIWD",
                        "IBOJVV",
                        "WHTZI",
                        "KUTWID",
                        "GBVEG",
                        "CJAUG",
                        "BBUBN",
                        "PNQXE",
                        "ZDLWZR",
                        "IHNNR",
                        "FKUIQ",
                        "ETQGI",
                        "RNZAT",
                        "AKVQL",
                        "CZOSY",
                        "TGABV",
                        "KYZJR",
                        "CWZIA",
                        "PZUDL",
                        "KIZHN",
                        "VUBMC",
                        "EZANX",
                        "NEWNX",
                        "CZSBW",
                        "AQHZZV",
                        "XVSDR",
                        "SIIMK",
                        "QTXZS",
                        "VRGWI",
                        "JDBOUQ",
                        "CRXWU",
                        "SUGWE",
                        "AJMUU",
                        "DEONTE",
                        "BPEHQ",
                        "KNYIC",
                        "NTERMITTENT",
                        "XSOWK",
                        "OISLK",
                        "RSLFG",
                        "PUZSG",
                        "OFPPC",
                        "UKIQY",
                        "YOOZX",
                        "HLJLQ",
                        "WHBWB",
                        "DIAMQ",
                        "CZCJO",
                        "WDBWBD",
                        "JPYVE",
                        "JKJRRZ",
                        "XDCZY",
                        "GJOAG",
                        "OYVLZ",
                        "AOKODF",
                        "BOXTU",
                        "AUFJM",
                        "XIUME",
                        "AFBOE",
                        "QTFSL",
                        "LZCGA",
                        "YXITB",
                        "IHNSJ",
                        "ZHLPH",
                        "JTXDT",
                        "PZPGH",
                        "ACVYS",
                        "AYQPR",
                        "YLNRB",
                        "AINESETLE",
                        "UWKPA",
                        "TOQCK",
                        "OUUNGESTEC",
                        "ZDIHY",
                        "SMLTQ",
                        "OYFPW",
                        "UYMVO",
                        "BUIVR",
                        "LSGLT",
                        "AMRDZ",
                        "JPEZI",
                        "BVFOI",
                        "REYXPI",
                        "DDOEK",
                        "XTKYM",
                        "ZIQTA",
                        "UDBBW",
                        "VSEVMP",
                        "WFAKU",
                        "VKDAX",
                        "LYHPI",
                        "WVFDS",
                        "TTXJL"
                },
                5
        );
    }

    private void startTest(String morse, String[] dictionary, int answer) throws Exception {
        mOutQueue.clear();
        mOutQueue.add(morse);
        mOutQueue.add(String.valueOf(dictionary.length));
        Collections.addAll(mOutQueue, dictionary);
        mAnswer = answer;

        final Scanner scanner = PowerMockito.mock(Scanner.class);
        PowerMockito.whenNew(Scanner.class).withAnyArguments().thenReturn(scanner);
        PowerMockito.when(scanner.nextInt()).thenAnswer(invocation -> Integer.parseInt(mOutQueue.pop()));
        PowerMockito.when(scanner.next()).thenAnswer(invocation -> mOutQueue.pop());

        PrintStream printStream = Mockito.mock(PrintStream.class);
        Mockito.doAnswer(invocation -> {
            final String answer1 = invocation.getArguments()[0].toString();
            mOriginalOutPrintStream.println(answer1);
            Assert.assertEquals(mAnswer, Integer.parseInt(answer1));

            throw new SuccessException();
        }).when(printStream).println(Mockito.anyInt());
        System.setOut(printStream);

        Solution.main(null);
    }

    private static class SuccessException extends Exception {

    }
}
