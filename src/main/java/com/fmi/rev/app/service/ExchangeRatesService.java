package com.fmi.rev.app.service;

import com.fmi.rev.app.exception.ExternalApiErrorException;
import com.fmi.rev.app.model.Currency;
import javafx.util.Pair;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeRatesService {
    private static final String EXCHANGE_RATES_API_URL = "https://api.getgeoapi.com/v2/currency/convert";
    private static final long EXCHANGE_RATE_OUTDATED_AFTER_SECONDS = 60;
    @Value("${exchange_rates_api_key}")
    private String exchangeRatesApiKey;
    private final ConcurrentHashMap<Pair<Currency, Currency>, Pair<Double, LocalDateTime>> exchangeRates = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public Double getExchangeRate(Currency from, Currency to) {
        if (from == to) {
            return 1.0;
        }
        Pair<Currency, Currency> key = new Pair<>(from, to);
        if (!exchangeRates.containsKey(key)) {
            return updateExchangeRate(from, to);
        }
        Pair<Double, LocalDateTime> value = exchangeRates.get(key);
        Double rate = value.getKey();
        LocalDateTime lastUpdateTime = value.getValue();
        long secondsPassed = ChronoUnit.SECONDS.between(lastUpdateTime, LocalDateTime.now());
        return secondsPassed < EXCHANGE_RATE_OUTDATED_AFTER_SECONDS ? rate : updateExchangeRate(from, to);
    }

    private Double updateExchangeRate(Currency from, Currency to) {
        String url = String.format("%s?api_key=%s&format=json&from=%s&to=%s", EXCHANGE_RATES_API_URL, exchangeRatesApiKey, from, to);
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            throw new ExternalApiErrorException();
        }
        JSONObject json = (JSONObject) JSONValue.parse(response);
        Object rateField = getJsonField((JSONObject) getJsonField((JSONObject) getJsonField(json, "rates"), to.toString()), "rate");
        if (rateField == null) {
            throw new ExternalApiErrorException(response);
        }
        Double rate = Double.valueOf(rateField.toString());
        exchangeRates.put(new Pair<>(from, to), new Pair<>(rate, LocalDateTime.now()));
        return rate;
    }

    private Object getJsonField(JSONObject json, String field) {
        if (json == null) {
            return null;
        }
        return json.get(field);
    }
}
