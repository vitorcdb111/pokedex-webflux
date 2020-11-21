package com.webflux.pokedex.controller;

import com.webflux.pokedex.model.Pokemon;
import com.webflux.pokedex.model.PokemonEvent;
import com.webflux.pokedex.repository.PokemonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/pokemons")
public class PokemonController {

    private PokemonRepository repository;
    public PokemonController(PokemonRepository repository) {this.repository = repository;}

    @GetMapping
    public Flux<Pokemon> getAllPokemons() {return repository.findAll();}

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Pokemon>> getPokemon(@PathVariable String id) {
        return repository.findById(id)
                .map(pokemon -> ResponseEntity.ok(pokemon))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Pokemon> savePokemon (@RequestBody Pokemon pokemon) {
        return repository.save(pokemon);
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<Pokemon>> uptadePokemon(@PathVariable(value = "id") String id, @RequestBody Pokemon pokemon) {
        return repository.findById(id)
                .flatMap(existingPokemon -> {
                    existingPokemon.setNome(pokemon.getNome());
                    existingPokemon.setCategoria(pokemon.getCategoria());
                    existingPokemon.setHabilidade(pokemon.getHabilidade());
                    existingPokemon.setPeso(pokemon.getPeso());
                    return repository.save(existingPokemon);
                })
                .map(uptadePokemon -> ResponseEntity.ok(uptadePokemon))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Object>> deletePokemon (@PathVariable(value = "id") String id) {
        return repository.findById(id)
                .flatMap(existingPokemon ->
                        repository.delete(existingPokemon)
                        .then(Mono.just(ResponseEntity.ok().build())))
                        .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping()
    public Mono<Void> deleteAllPokemon (@PathVariable(value = "id") String id) {
        return repository.deleteAll();
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PokemonEvent> getPokemonEvents() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(val -> new PokemonEvent(val, "Pokemons"));
    }

}