package com.fanshuai.io;

import lombok.Data;

import java.util.Objects;

@Data
public class IpAndPort {
    private String ip;
    private int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IpAndPort ipAndPort = (IpAndPort) o;
        return port == ipAndPort.port &&
                Objects.equals(ip, ipAndPort.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ip, port);
    }
}
