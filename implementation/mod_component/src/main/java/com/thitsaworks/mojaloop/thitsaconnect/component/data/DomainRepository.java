package com.thitsaworks.mojaloop.thitsaconnect.component.data;

public interface DomainRepository <ID, DATA> {

    void save(DATA domain);

    DATA get(ID id);

    void delete(ID id);
}
