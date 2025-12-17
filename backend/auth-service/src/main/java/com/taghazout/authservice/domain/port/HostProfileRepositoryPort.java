package com.taghazout.authservice.domain.port;

import com.taghazout.authservice.domain.entity.HostProfile;

public interface HostProfileRepositoryPort {
    HostProfile save(HostProfile hostProfile);
}
