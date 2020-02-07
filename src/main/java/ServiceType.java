public enum ServiceType {

    // Wniosek o legalizację pobytu - http://rezerwacje.duw.pl/reservations/pol/queues/17/1
    // Oddział LP I dni rezerwacji -  wt 13-15 i czw 13-15 - http://rezerwacje.duw.pl/reservations/pol/queues/103/4
    // Oddział LP II dni rezerwacji -  wt 13-15 i czw 13-15 - http://rezerwacje.duw.pl/reservations/pol/queues/62/6
    // Dyrektor Wydziału rezerwacje - wt 10-12 i śr 16-16.30 - http://rezerwacje.duw.pl/reservations/pol/queues/500000019/25

    WNIOSEK_O_LEGALIZACJĘ_POBYTU(17, 1),
    ODDZIAŁ_LP_I_DNI_REZERWACJI(103, 4),
    ODDZIAŁ_LP_II_DNI_REZERWACJI(62, 6),
    DYREKTOR_WYDZIAŁU_REZERWACJE(500000019, 25);

    int val1, val2;

    ServiceType(int val1, int val2) {
        this.val1 = val1;
        this.val2 = val2;
    }

}
