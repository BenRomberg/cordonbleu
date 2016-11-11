package com.benromberg.cordonbleu.data.model;

import com.benromberg.cordonbleu.data.model.User;

public interface UserFixture extends TeamFixture {
    String USER_PASSWORD = "user-password";
    String USER_NAME = "user-name";
    String USER_EMAIL = "user@email.com";
    User USER = new UserBuilder().build();
    String USER_ID = USER.getId();

    default UserBuilder user() {
        return new UserBuilder();
    }

    class UserBuilder {
        private String name = USER_NAME;
        private String email = USER_EMAIL;
        private String password = USER_PASSWORD;

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            return new User(email, name, password);
        }
    }
}
