package by.ladyka.family.services;

import by.ladyka.family.entity.MarriageEntity;
import by.ladyka.family.entity.Person;
import by.ladyka.family.entity.RelationType;
import by.ladyka.family.repositories.MarriageRepository;
import by.ladyka.family.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonRelationService {

    private final MarriageRepository marriageRepository;
    private final PersonRepository personRepository;

    public void createRelation(Long id1, Long id2, RelationType relation) {
        Person two = this.personRepository.getOne(id2);
        Person one = this.personRepository.getOne(id1);

        if (RelationType.MARRIAGE.equals(relation)) {
            MarriageEntity marriageEntity = new MarriageEntity();
            if (one.getGender().compareTo(two.getGender()) > 0) {
                marriageEntity.setHusband(one);
                marriageEntity.setWife(two);
            } else {
                marriageEntity.setHusband(two);
                marriageEntity.setWife(one);
            }
            marriageRepository.save(marriageEntity);
        } else {
            if (two.getBirthday().isAfter(one.getBirthday())) {
                MarriageEntity marriage;
                if (one.getGender()) {
                    marriage = marriageRepository.findByHusband(one).get(0);
                } else {
                    marriage = marriageRepository.findByWife(one).get(0);
                }
                two.setMarriage(marriage);
                personRepository.save(two);
            } else {
                MarriageEntity marriage;
                if (two.getGender()) {
                    marriage = marriageRepository.findByHusband(two).get(0);
                } else {
                    marriage = marriageRepository.findByWife(two).get(0);
                }
                one.setMarriage(marriage);
                personRepository.save(one);
            }
        }
    }

    public List<Person> husbands(Person person) {
        return marriageRepository.findByWife(person)
                .stream()
                .map(MarriageEntity::getHusband)
                .collect(Collectors.toList());
    }

    public List<Person> wives(Person person) {
        return marriageRepository.findByHusband(person)
                .stream()
                .map(MarriageEntity::getWife)
                .collect(Collectors.toList());
    }

    public Optional<Person> getFather(Person me) {
        return Optional.of(me)
                .map(Person::getMarriage)
                .map(MarriageEntity::getHusband);
    }

    public Optional<Person> getMother(Person me) {
        return Optional.of(me)
                .map(Person::getMarriage)
                .map(MarriageEntity::getWife);
    }

    public List<Person> getChildren(Person husband, Person wife) {
        return marriageRepository.findByHusbandAndWife(husband, wife)
                .map(MarriageEntity::getChildren)
                .orElse(new ArrayList<>());
    }

    public List<Person> getChildren(Person person) {
        List<MarriageEntity> marriages;
        if (person.getGender()) {
            marriages = marriageRepository.findByHusband(person);
        } else {
            marriages = marriageRepository.findByWife(person);
        }
        return marriages.stream()
                .map(MarriageEntity::getChildren)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
