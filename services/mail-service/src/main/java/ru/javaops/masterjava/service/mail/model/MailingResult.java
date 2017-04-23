package ru.javaops.masterjava.service.mail.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.javaops.masterjava.persist.model.BaseEntity;

/**
 * Created by demi
 * on 21.04.17.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class MailingResult extends BaseEntity {
	private @NonNull MailingResultType value;
	private @NonNull String to;
	private @NonNull String cc;
	private @NonNull String subject;
}
