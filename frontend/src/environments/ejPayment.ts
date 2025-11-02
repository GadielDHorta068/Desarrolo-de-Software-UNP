// MODELOS
export interface Payment {
  id: number;
  date_created: string;
  date_approved: string;
  date_last_updated: string;
  money_release_status: string;
  operation_type: string;
  status: string;
  status_detail: string;
  currency_id: string;
  transaction_amount: number;
  installments: number;
  payer: Payer;
  payment_method: PaymentMethod;
  transaction_details: TransactionDetails;
  fee_details: FeeDetail[];
  card: Card;
}

export interface Payer {
  id: string;
  email: string;
  identification: Identification;
}

export interface Identification {
  type: string;
  number: string;
}

export interface PaymentMethod {
  id: string;
  type: string;
  issuer_id: string;
}

export interface TransactionDetails {
  net_received_amount: number;
  total_paid_amount: number;
  installment_amount: number;
}

export interface FeeDetail {
  type: string;
  amount: number;
  fee_payer: string;
}

export interface Card {
  first_six_digits: string;
  last_four_digits: string;
  expiration_month: number;
  expiration_year: number;
  country: string;
  tags: string[];
  cardholder: Cardholder;
}

export interface Cardholder {
  name: string;
  identification: Identification;
}


// recibido del FRONT
// {token=19f62829fe96b1cdb99a5d5a1d4a431d, issuer_id=1, payment_method_id=debvisa, transaction_amount=100.0, installments=1, payer={email=jhon@doe.com, identification={type=DNI, number=12345678}}}

// pago creado por api MP
const pago: Payment = {
  id: 1325258616,
  date_created: "2025-11-01T09:50:25.951-04:00",
  date_approved: "2025-11-01T09:50:26.427-04:00",
  date_last_updated: "2025-11-01T09:50:26.427-04:00",
  money_release_status: "pending",
  operation_type: "regular_payment",
  status: "approved",
  status_detail: "accredited",
  currency_id: "ARS",
  transaction_amount: 100,
  installments: 1,
  payer: {
    id: "1693648451",
    email: "test_user_80507629@testuser.com",
    identification: { type: "DNI", number: "32659430" }
  },
  payment_method: {
    id: "debvisa",
    type: "debit_card",
    issuer_id: "1"
  },
  transaction_details: {
    net_received_amount: 95.9,
    total_paid_amount: 100,
    installment_amount: 100
  },
  fee_details: [
    { type: "mercadopago_fee", amount: 4.1, fee_payer: "collector" }
  ],
  card: {
    first_six_digits: "400276",
    last_four_digits: "5619",
    expiration_month: 11,
    expiration_year: 2030,
    country: "ARG",
    tags: ["debit"],
    cardholder: {
      name: "APRO",
      identification: { type: "DNI", number: "12345678" }
    }
  }
};
