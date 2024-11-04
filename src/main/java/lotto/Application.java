package lotto;

import camp.nextstep.edu.missionutils.Console;
import camp.nextstep.edu.missionutils.Randoms;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {

    private static final int LOTTO_PRICE = 1000;
    private static final int LOTTO_NUMBER_COUNT = 6;
    private static final int MIN_LOTTO_NUMBER = 1;
    private static final int MAX_LOTTO_NUMBER = 45;
    private static final int[] PRIZES = {0, 2000000000, 30000000, 1500000, 50000, 5000};

    public enum Rank {
        FIRST(6, false, 2_000_000_000),
        SECOND(5, true, 30_000_000),
        THIRD(5, false, 1_500_000),
        FOURTH(4, false, 50_000),
        FIFTH(3, false, 5_000),
        NONE(0, false, 0);

        private final int matchCount;
        private final boolean matchBonus;
        private final int prize;

        Rank(int matchCount, boolean matchBonus, int prize) {
            this.matchCount = matchCount;
            this.matchBonus = matchBonus;
            this.prize = prize;
        }

        public int getPrize() {
            return prize;
        }

        public static Rank valueOf(int matchCount, boolean matchBonus) {
            for (Rank rank : values()) {
                if (rank.matches(matchCount, matchBonus)) {
                    return rank;
                }
            }
            return NONE;
        }

        private boolean matches(int matchCount, boolean matchBonus) {
            return this.matchCount == matchCount && (!this.matchBonus || matchBonus);
        }
    }

    public static void main(String[] args) {

        int purchaseAmount = inputPurchaseAmount();
        int numberOfLottos = purchaseAmount / 1000;
        List<Lotto> lottos = generateLottos(numberOfLottos);
        printLottos(lottos);

        List<Integer> winningNumbers = inputWinningNumbers();
        int bonusNumber = inputBonusNumber();

        Map<Rank, Integer> results = calculateResults(lottos, winningNumbers, bonusNumber);
        printResults(results, purchaseAmount);
    }

    public static int inputPurchaseAmount() {
        while (true) {
            try {
                System.out.println("구입금액을 입력해 주세요.");
                String input = Console.readLine();
                int amount = Integer.parseInt(input);
                validatePurchaseAmount(amount);

                return amount;
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] 금액은 숫자로 입력해 주세요.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static List<Lotto> generateLottos(int count) {
        List<Lotto> lottos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            List<Integer> numbers = Randoms.pickUniqueNumbersInRange(MIN_LOTTO_NUMBER, MAX_LOTTO_NUMBER, LOTTO_NUMBER_COUNT);
            Lotto lotto = new Lotto(numbers);
            lottos.add(lotto);
        }
        return lottos;
    }

    public static void printLottos(List<Lotto> lottos) {
        System.out.println(lottos.size() + "개를 구매했습니다.");
        for (Lotto lotto : lottos) {
            List<Integer> numbers = new ArrayList<>(lotto.getNumbers());
            Collections.sort(numbers);
            System.out.println(numbers);
        }
    }

    public static List<Integer> inputWinningNumbers() {
        while (true) {
            try {
                System.out.println("당첨 번호를 입력해 주세요.");
                String input = Console.readLine();
                String[] inputs = input.split(",");
                if (inputs.length != LOTTO_NUMBER_COUNT) {
                    throw new IllegalArgumentException("[ERROR] 당첨 번호는 6개여야 합니다.");
                }
                List<Integer> winningNumbers = new ArrayList<>();
                for (String number : inputs) {
                    int num = Integer.parseInt(number.trim());
                    validateNumber(num);
                    if (winningNumbers.contains(num)) {
                        throw new IllegalArgumentException("[ERROR] 중복된 번호는 입력할 수 없습니다.");
                    }
                    winningNumbers.add(num);
                }
                return winningNumbers;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static int inputBonusNumber() {
        while (true) {
            try {
                System.out.println("보너스 번호를 입력해 주세요.");
                String input = Console.readLine();
                int bonusNumber = Integer.parseInt(input);
                validateNumber(bonusNumber);
                return bonusNumber;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void validatePurchaseAmount(int amount) {
        if (amount < LOTTO_PRICE) {
            throw new IllegalArgumentException("[ERROR] 구입 금액은 최소 1,000원 이상이어야 합니다.");
        }
        if (amount % LOTTO_PRICE != 0) {
            throw new IllegalArgumentException("[ERROR] 구입 금액은 1,000원 단위여야 합니다.");
        }
    }

    public static void validateNumber(int number) {
        if (number < MIN_LOTTO_NUMBER || number > MAX_LOTTO_NUMBER) {
            throw new IllegalArgumentException("[ERROR] 번호는 1부터 45 사이의 숫자여야 합니다.");
        }
    }

    public static Map<Rank, Integer> calculateResults(List<Lotto> lottos, List<Integer> winningNumbers,
            int bonusNumber) {
        Map<Rank, Integer> resultMap = new HashMap<>();
        for (Lotto lotto : lottos) {
            int matchCount = getMatchCount(lotto.getNumbers(), winningNumbers);
            boolean bonusMatch = lotto.getNumbers().contains(bonusNumber);
            Rank rank = Rank.valueOf(matchCount, bonusMatch);
            resultMap.put(rank, resultMap.getOrDefault(rank, 0) + 1);
        }
        return resultMap;
    }

    public static int getMatchCount(List<Integer> numbers, List<Integer> winningNumbers) {
        int count = 0;
        for (int number : numbers) {
            if (winningNumbers.contains(number)) {
                count++;
            }
        }
        return count;
    }

    public static void printResults(Map<Rank, Integer> results, int purchaseAmount) {
        System.out.println("당첨 통계\n---");
        int totalPrize = 0;

        for (Rank rank : Rank.values()) {
            if (rank == Rank.NONE) {
                continue;
            }
            int count = results.getOrDefault(rank, 0);
            totalPrize += count * rank.getPrize();
            printRankResult(rank, count);
        }

        double profit = ((double) totalPrize / purchaseAmount) * 100;
        profit = Math.round(profit * 10) / 10.0;
        System.out.println("총 수익률은 " + profit + "%입니다.");
    }


    public static void printRankResult(Rank rank, int count) {
        if (rank == Rank.SECOND) {
            System.out.println("5개 일치, 보너스 볼 일치 (" + rank.getPrize() + "원) - " + count + "개");
            return;
        }
        System.out.println(rank.matchCount + "개 일치 (" + rank.getPrize() + "원) - " + count + "개");
    }
}
