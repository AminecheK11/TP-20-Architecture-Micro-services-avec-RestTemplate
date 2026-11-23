package com.example.client.services;
import com.example.client.entities.Client;
import com.example.client.repositories.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClientService {
    private final ClientRepository repository;
    public ClientService(ClientRepository repository) { this.repository = repository; }
    public List<Client> findAll() { return repository.findAll(Sort.by("id")); }
    public Client findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));
    }
    @Transactional
    public Client addClient(Client client) {
        if(client.getNom() == null || client.getNom().isBlank() || client.getNom().length() > 255 ||
            client.getAge() == null || !Float.isFinite(client.getAge()) || client.getAge() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom et âge valides requis");
        // Un POST crée un client ; l'id fourni ne doit pas modifier une ligne existante.
        return repository.save(new Client(client.getNom(), client.getAge()));
    }
    @Transactional
    public void initialiserSiVide() {
        if(repository.count() == 0) repository.saveAll(List.of(
            new Client("Rabab SELIMANI", 23f), new Client("Amal RAMI", 22f), new Client("Samir SAFI", 22f)));
    }
}
