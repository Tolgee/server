package io.tolgee.service;

import io.tolgee.collections.LanguageSet;
import io.tolgee.constants.Message;
import io.tolgee.dtos.request.LanguageDTO;
import io.tolgee.exceptions.NotFoundException;
import io.tolgee.model.Language;
import io.tolgee.model.Repository;
import io.tolgee.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final EntityManager entityManager;

    private TranslationService translationService;

    @Autowired
    public LanguageService(LanguageRepository languageRepository, EntityManager entityManager) {
        this.languageRepository = languageRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Language createLanguage(LanguageDTO dto, Repository repository) {
        Language language = Language.fromRequestDTO(dto);
        language.setRepository(repository);
        repository.getLanguages().add(language);
        languageRepository.save(language);
        return language;
    }

    @Transactional
    public void deleteLanguage(Long id) {
        Language language = languageRepository.findById(id).orElseThrow(NotFoundException::new);
        translationService.deleteAllByLanguage(language.getId());
        languageRepository.delete(language);
    }

    @Transactional
    public Language editLanguage(LanguageDTO dto) {
        Language language = languageRepository.findById(dto.getId()).orElseThrow(NotFoundException::new);
        language.updateByDTO(dto);
        entityManager.persist(language);
        return language;
    }

    public LanguageSet getImplicitLanguages(Repository repository) {
        return repository.getLanguages().stream().limit(2).collect(Collectors.toCollection(LanguageSet::new));
    }

    @Transactional
    public LanguageSet findAll(Long repositoryId) {
        return new LanguageSet(languageRepository.findAllByRepositoryId(repositoryId));
    }

    public Optional<Language> findById(Long id) {
        return languageRepository.findById(id);
    }

    public Optional<Language> findByAbbreviation(String abbreviation, Repository repository) {
        return languageRepository.findByAbbreviationAndRepository(abbreviation, repository);
    }

    public Optional<Language> findByAbbreviation(String abbreviation, Long repositoryId) {
        return languageRepository.findByAbbreviationAndRepositoryId(abbreviation, repositoryId);
    }

    public LanguageSet findByAbbreviations(Collection<String> abbreviations, Long repositoryId) {
        Set<Language> langs = languageRepository.findAllByAbbreviationInAndRepositoryId(abbreviations, repositoryId);
        if (!langs.stream().map(Language::getAbbreviation).collect(Collectors.toSet()).containsAll(abbreviations)) {
            throw new NotFoundException(Message.LANGUAGE_NOT_FOUND);
        }
        return new LanguageSet(langs);
    }

    @Transactional
    public Language getOrCreate(Repository repository, String languageAbbreviation) {
        return this.findByAbbreviation(languageAbbreviation, repository)
                .orElseGet(() -> this.createLanguage(new LanguageDTO(null, languageAbbreviation, languageAbbreviation), repository));
    }

    public LanguageSet getLanguagesForTranslationsView(Set<String> languages, Repository repository) {
        if (languages == null) {
            return getImplicitLanguages(repository);
        }
        return findByAbbreviations(languages, repository.getId());
    }

    public Optional<Language> findByName(String name, Repository repository) {
        return languageRepository.findByNameAndRepository(name, repository);
    }

    public void deleteAllByRepository(Long repositoryId) {
        languageRepository.deleteAllByRepositoryId(repositoryId);
    }

    @Autowired
    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }
}
