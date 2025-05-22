package psw.verapelle.entity;

public enum OrderStatus {
    CREATED,    // appena generato
    PAID,       // pagamento andato a buon fine
    DECLINED,   // pagamento rifiutato
    CANCELLED   // ordine annullato
}
